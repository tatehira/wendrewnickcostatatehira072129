package com.wendrewnick.musicmanager.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RateLimitFilter implements Filter {

    private static final int CLEANUP_INTERVAL_MINUTES = 5;
    private static final long BUCKET_TTL_MINUTES = 10;

    private final Map<String, BucketInfo> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleanup");
        t.setDaemon(true);
        return t;
    });

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit.requests-per-minute:10}")
    private int requestsPerMinute;

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredBuckets, 
                CLEANUP_INTERVAL_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
        log.info("Rate limit inicializado: {} requisições por minuto", requestsPerMinute);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        
        if (!rateLimitEnabled) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        
        if (shouldSkipRateLimit(requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        try {
            String key = resolveKey(request);
            BucketInfo bucketInfo = cache.computeIfAbsent(key, k -> new BucketInfo(createNewBucket(), System.currentTimeMillis()));
            bucketInfo.lastAccessTime = System.currentTimeMillis();

            ConsumptionProbe probe = bucketInfo.bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                addRateLimitHeaders(response, probe);
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                handleRateLimitExceeded(response, probe);
            }
        } catch (Exception e) {
            log.error("Erro no rate limit para URI: {}", requestURI, e);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean shouldSkipRateLimit(String requestURI) {
        return requestURI.startsWith("/swagger-ui") 
                || requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/api-docs")
                || requestURI.startsWith("/actuator/health")
                || requestURI.equals("/");
    }

    private void addRateLimitHeaders(HttpServletResponse response, ConsumptionProbe probe) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        long resetTime = (System.currentTimeMillis() / 1000) + 60;
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
    }

    private void handleRateLimitExceeded(HttpServletResponse response, ConsumptionProbe probe) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        long retryAfter = 60;
        long resetTime = (System.currentTimeMillis() / 1000) + retryAfter;
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));

        String body = String.format("""
                {"title":"Limite de requisições excedido","detail":"Máximo de %d requisições por minuto por usuário. Tente novamente em %d segundos.","status":429,"retryAfter":%d}""",
                requestsPerMinute, retryAfter, retryAfter);
        
        response.getWriter().write(body);
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String resolveKey(HttpServletRequest request) {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof String && !"anonymousUser".equals(principal)) {
                    return "user:" + principal;
                }
            }
        } catch (Exception e) {
            log.debug("Erro ao resolver chave do usuário, usando IP", e);
        }
        
        return "ip:" + getClientIpAddress(request);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private void cleanupExpiredBuckets() {
        long expireTime = BUCKET_TTL_MINUTES * 60 * 1000;
        long now = System.currentTimeMillis();
        
        cache.entrySet().removeIf(entry -> {
            long lastAccess = entry.getValue().lastAccessTime;
            return (now - lastAccess) > expireTime;
        });
    }

    @Override
    public void destroy() {
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        cache.clear();
    }

    private static class BucketInfo {
        final Bucket bucket;
        volatile long lastAccessTime;

        BucketInfo(Bucket bucket, long lastAccessTime) {
            this.bucket = bucket;
            this.lastAccessTime = lastAccessTime;
        }
    }
}

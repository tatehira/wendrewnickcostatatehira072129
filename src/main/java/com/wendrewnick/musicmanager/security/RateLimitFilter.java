package com.wendrewnick.musicmanager.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private static final int LIMITE_POR_MINUTO = 10;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        
        if (uri.startsWith("/swagger") || uri.contains("api-docs")) {
            chain.doFilter(req, res);
            return;
        }

        String chave = obterChave(request);
        Bucket bucket = buckets.computeIfAbsent(chave, k -> criarBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"title\":\"Rate limit exceeded\",\"detail\":\"Limite de " + LIMITE_POR_MINUTO + " requisições por minuto excedido\",\"status\":429}");
        }
    }

    private Bucket criarBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(LIMITE_POR_MINUTO)
                        .refillGreedy(LIMITE_POR_MINUTO, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private String obterChave(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return request.getRemoteAddr();
    }
}

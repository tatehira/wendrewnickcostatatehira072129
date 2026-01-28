package com.wendrewnick.musicmanager.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGlobalException(Exception e) {
        log.error("Unhandled exception", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Consulte os logs do servidor.");
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setTitle("Recurso Não Encontrado");
        problemDetail.setType(URI.create("urn:problem-type:resource-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        String detail = "Falha na validação dos dados: " + String.join(", ", errors.values());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Erro de Validação");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());

        log.warn("Erro de validação: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Usuário ou senha inválidos");
        problemDetail.setTitle("Falha na Autenticação");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problemDetail.setTitle("Refresh token inválido");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Acesso Negado");
        problemDetail.setTitle("Falha na Autorização");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("Operação não permitida");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ProblemDetail> handlePropertyReferenceException(PropertyReferenceException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("Parâmetro de ordenação inválido");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
                "O arquivo excede o tamanho máximo permitido de 10MB.");
        problemDetail.setTitle("Arquivo muito grande");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(problemDetail);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ProblemDetail> handleStorageException(StorageException e) {
        log.error("Erro no armazenamento: {}", e.getMessage(), e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                e.getMessage() != null ? e.getMessage() : "Erro ao processar arquivo. Tente novamente mais tarde.");
        problemDetail.setTitle("Erro no Armazenamento");
        problemDetail.setProperty("timestamp", Instant.now());
        if (e.getCause() != null) {
            problemDetail.setProperty("cause", e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ProblemDetail> handleExternalApiException(ExternalApiException e) {
        log.error("Erro na API externa", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Erro ao comunicar com serviço externo. Tente novamente mais tarde.");
        problemDetail.setTitle("Erro na API Externa");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problemDetail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Violação de integridade de dados", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Operação não permitida: violação de regra de negócio (ex: registro duplicado).");
        problemDetail.setTitle("Conflito de Dados");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> handleDataAccessException(DataAccessException e) {
        log.error("Erro de acesso ao banco de dados", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Erro ao acessar o banco de dados. Tente novamente mais tarde.");
        problemDetail.setTitle("Erro no Banco de Dados");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Argumento inválido", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                e.getMessage() != null ? e.getMessage() : "Parâmetro inválido fornecido.");
        problemDetail.setTitle("Parâmetro Inválido");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("Tipo de argumento inválido", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Formato de parâmetro inválido. Verifique o tipo de dados enviado.");
        problemDetail.setTitle("Tipo de Parâmetro Inválido");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Mensagem HTTP não legível", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Formato de dados inválido. Verifique o JSON enviado.");
        problemDetail.setTitle("Formato de Dados Inválido");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ProblemDetail> handleJsonProcessingException(JsonProcessingException e) {
        log.error("Erro ao processar JSON", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Erro ao processar dados JSON. Verifique o formato.");
        problemDetail.setTitle("Erro no Processamento JSON");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("Parâmetro obrigatório ausente", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: " + e.getParameterName());
        problemDetail.setTitle("Parâmetro Obrigatório Ausente");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ProblemDetail> handleMultipartException(MultipartException e) {
        log.warn("Erro ao processar multipart", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Erro ao processar arquivo. Verifique se está enviando multipart/form-data corretamente.");
        problemDetail.setTitle("Erro no Upload de Arquivo");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.warn("Tipo de mídia não suportado: {}. Content-Type recebido: {}", e.getSupportedMediaTypes(), e.getContentType(), e);
        String supportedTypes = e.getSupportedMediaTypes() != null && !e.getSupportedMediaTypes().isEmpty() 
                ? String.join(", ", e.getSupportedMediaTypes().stream().map(MediaType::toString).toList())
                : "multipart/form-data ou application/json";
        
        String detail = "Tipo de conteúdo não suportado. Recebido: " + (e.getContentType() != null ? e.getContentType() : "não especificado") 
                + ". Use: " + supportedTypes;
        
        if (e.getContentType() != null && e.getContentType().toString().equals("application/octet-stream")) {
            detail += ". DICA: Para criar álbum com imagens, use multipart/form-data com campos 'data' (JSON) e 'images' (arquivos). Para criar sem imagens, use o endpoint POST /api/v1/albums/simple com Content-Type: application/json.";
        }
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, detail);
        problemDetail.setTitle("Tipo de Mídia Não Suportado");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("receivedContentType", e.getContentType() != null ? e.getContentType().toString() : null);
        problemDetail.setProperty("supportedTypes", supportedTypes);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(problemDetail);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Método HTTP não suportado", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + e.getMethod() + " não é suportado para este endpoint.");
        problemDetail.setTitle("Método Não Permitido");
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problemDetail);
    }
}

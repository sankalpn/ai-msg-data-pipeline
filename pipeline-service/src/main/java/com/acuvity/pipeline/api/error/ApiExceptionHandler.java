package com.acuvity.pipeline.api;

import com.acuvity.pipeline.models.ApiError;
import com.acuvity.pipeline.service.redpanda.PublishException;
import com.acuvity.pipeline.service.sync.SyncFanOutException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage())
                .toList();
        return error(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    ResponseEntity<ApiError> validation(WebExchangeBindException exception) {
        List<String> details = exception.getAllErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage())
                .toList();
        return error(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler({ConstraintViolationException.class, InvalidRequestException.class})
    ResponseEntity<ApiError> invalidRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, List.of(exception.getMessage()));
    }

    @ExceptionHandler(NamespaceNotEnabledException.class)
    ResponseEntity<ApiError> namespaceNotEnabled(NamespaceNotEnabledException exception) {
        return error(HttpStatus.FORBIDDEN, List.of(exception.getMessage()));
    }

    @ExceptionHandler(WebClientException.class)
    ResponseEntity<ApiError> logServiceFailure(WebClientException exception) {
        return error(HttpStatus.BAD_GATEWAY, List.of("Log service request failed"));
    }

    @ExceptionHandler(SyncFanOutException.class)
    ResponseEntity<ApiError> fanOutFailure(SyncFanOutException exception) {
        return error(HttpStatus.BAD_GATEWAY, List.of(exception.getMessage()));
    }

    @ExceptionHandler(PublishException.class)
    ResponseEntity<ApiError> publishFailure(PublishException exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, List.of(exception.getMessage()));
    }

    private ResponseEntity<ApiError> error(HttpStatus status, List<String> details) {
        return ResponseEntity.status(status).body(
                new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), details));
    }
}

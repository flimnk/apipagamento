package edu.ucsal.fiadopay.infra.Exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private record ErrorDetails(
            Instant timestamp,
            HttpStatus status,
            String error,
            String message,
            String path
    ) {}

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ErrorDetails> handleConflictExceptions(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT; // 409 Conflict
        return ResponseEntity.status(status).body(new ErrorDetails(
                Instant.now(),
                status,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getRequestURI()
        ));
    }
}

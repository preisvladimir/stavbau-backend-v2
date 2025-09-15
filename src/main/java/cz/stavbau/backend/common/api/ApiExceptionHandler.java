package cz.stavbau.backend.common.api;

import cz.stavbau.backend.integrations.ares.exceptions.AresNotFoundException;
import cz.stavbau.backend.integrations.ares.exceptions.AresUnavailableException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.BindException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ProblemDetail;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BindException.class)
    public ProblemDetail handleValidation(BindException ex){
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed"); pd.setDetail(ex.getMessage());
        pd.setProperty("code","validation.error");
        return pd;
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleDenied(AccessDeniedException ex){
        var pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Forbidden"); pd.setDetail("Access denied"); pd.setProperty("code","auth.forbidden");
        return pd;
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleRse(ResponseStatusException ex){
        var pd = ProblemDetail.forStatus(ex.getStatusCode());
        pd.setTitle("Error"); pd.setDetail(ex.getReason()); pd.setProperty("code","generic.error");
        return pd;
    }
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex){
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Server error"); pd.setDetail("Unexpected error"); pd.setProperty("code","server.error");
        return pd;
    }

    @ExceptionHandler(AresNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleAresNotFound(AresNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error","ares_not_found","message",ex.getMessage()));
    }

    @ExceptionHandler(AresUnavailableException.class)
    public ResponseEntity<Map<String,Object>> handleAresUnavailable(AresUnavailableException ex) {
        return ResponseEntity.status(503).body(Map.of("error","ares_unavailable","message",ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of("error","validation_error","message",
                ex.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
    }
}

package cz.stavbau.backend.features.registrations.api.error;

import cz.stavbau.backend.features.registrations.service.exceptions.RegistrationExceptions.*;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice(assignableTypes = cz.stavbau.backend.features.registrations.api.RegistrationController.class)
public class RegistrationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = base("validation.failed", "Invalid request");
        Map<String, Object> details = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put("field", fe.getField());
            details.put("violations", List.of(
                    fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()
            ));
            break; // 1. chyba stačí
        }
        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RateLimited.class)
    public ResponseEntity<Map<String, Object>> handleRate(RateLimited ex) {
        Map<String, Object> body = base("rateLimit.exceeded", "Too many requests");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, "300")
                .body(body);
    }

    @ExceptionHandler(NotFound.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(base(ex.getMessage(), "Not found"));
    }

    @ExceptionHandler(TokenInvalidOrExpired.class)
    public ResponseEntity<Map<String, Object>> handleToken(TokenInvalidOrExpired ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(base(ex.getMessage(), "Invalid or expired token"));
    }

    @ExceptionHandler(AlreadyConfirmed.class)
    public ResponseEntity<Map<String, Object>> handleAlready(AlreadyConfirmed ex) {
        return ResponseEntity.ok(base(ex.getMessage(), "Already confirmed"));
    }

    @ExceptionHandler(Conflict.class)
    public ResponseEntity<Map<String, Object>> handleConflict(Conflict ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(base(ex.getMessage(), "Conflict"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegal(IllegalStateException ex) {
        // Používáme např. pro mail.send.failed nebo rateLimit.exceeded fallback
        String code = ex.getMessage() != null ? ex.getMessage() : "internal";
        HttpStatus status = "rateLimit.exceeded".equals(code) ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(base(code, "Internal error"));
    }

    private Map<String, Object> base(String code, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", code);
        m.put("message", message);
        m.put("traceId", UUID.randomUUID().toString()); // nahraď instrumentací, pokud máš
        m.put("timestamp", OffsetDateTime.now().toString());
        return m;
    }
}

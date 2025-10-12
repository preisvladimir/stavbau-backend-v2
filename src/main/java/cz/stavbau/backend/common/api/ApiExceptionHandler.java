package cz.stavbau.backend.common.api;

import cz.stavbau.backend.common.exception.ForbiddenException;
import cz.stavbau.backend.common.exception.ValidationException;
import cz.stavbau.backend.integrations.ares.exceptions.AresNotFoundException;
import cz.stavbau.backend.integrations.ares.exceptions.AresUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Jednotné RFC7807 chyby pro celé API.
 * - 403 RBAC: "rbac.forbidden" (+ requiredScopes pokud jsou k dispozici)
 * - 422 validační: { errors: { field: message } }
 * - integrace ARES: problems/integrations/ares/*
 * - traceId (MDC) kvůli observabilitě
 */
@ControllerAdvice
public class ApiExceptionHandler {

    // ---------- Helpers ------------------------------------------------------

    private ProblemDetail base(HttpStatus status, String title, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        if (detail != null && !detail.isBlank()) {
            pd.setDetail(detail);
        }
        if (req != null) {
            pd.setInstance(URI.create(req.getRequestURI()));
        }
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }
        return pd;
    }

    private static Map<String, Object> collectFieldErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
        return errors;
    }

    // ---------- 400 / 422: validation ---------------------------------------

    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBind(BindException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), req);
        pd.setProperty("code", "validation.error");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed", "Invalid request", req);
        pd.setProperty("code", "validation.failed");
        pd.setProperty("errors", collectFieldErrors(ex)); // FE → RHF mapování
        return pd;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex, Locale locale, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), req);
        pd.setProperty("code", "validation.error");
        pd.setProperty("violations", ex.getViolations());
        return pd;
    }

    // ---------- 403: RBAC ----------------------------------------------------

    /** Doménová 403 (naše ForbiddenException) – nese requiredScopes. */
    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleDomainForbidden(ForbiddenException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.FORBIDDEN, "Forbidden", "Nemáte oprávnění provést tuto akci.", req);
        pd.setType(URI.create("https://stavbau.cz/problems/rbac/forbidden"));
        pd.setProperty("code", "rbac.forbidden");
        List<String> rs = ex.getRequiredScopes();
        if (rs != null && !rs.isEmpty()) {
            pd.setProperty("requiredScopes", rs);
        }
        return pd;
    }

    /**
     * Bezpečnostní 403 (Spring Security) – neznáme requiredScopes, ale můžeme je
     * doplnit přes request atribut (pokud je guard/filtr nastaví).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.FORBIDDEN, "Forbidden", "Nemáte oprávnění provést tuto akci.", req);
        pd.setType(URI.create("https://stavbau.cz/problems/rbac/forbidden"));
        pd.setProperty("code", "rbac.forbidden");
        Object required = req.getAttribute("requiredScopes");
        if (required instanceof List<?> list && !list.isEmpty()) {
            pd.setProperty("requiredScopes", list);
        }
        return pd;
    }

    // ---------- 404/503: ARES (RFC7807) --------------------------------------

    @ExceptionHandler(AresNotFoundException.class)
    public ProblemDetail handleAresNotFound(AresNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.NOT_FOUND, "ARES: company not found", ex.getMessage(), req);
        pd.setType(URI.create("https://stavbau.cz/problems/integrations/ares/not_found"));
        pd.setProperty("code", "integrations.ares.not_found");
        return pd;
    }

    @ExceptionHandler(AresUnavailableException.class)
    public ProblemDetail handleAresUnavailable(AresUnavailableException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.SERVICE_UNAVAILABLE, "ARES unavailable", ex.getMessage(), req);
        pd.setType(URI.create("https://stavbau.cz/problems/integrations/ares/unavailable"));
        pd.setProperty("code", "integrations.ares.unavailable");
        return pd;
    }

    // ---------- ResponseStatusException passthrough --------------------------

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleRse(ResponseStatusException ex, HttpServletRequest req) {
        ProblemDetail pd = base(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason() != null ? ex.getReason() : "Error",
                ex.getReason(),
                req
        );
        pd.setProperty("code", "generic.error");
        return pd;
    }

    // ---------- 500: catch-all -----------------------------------------------

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", "Unexpected error", req);
        pd.setProperty("code", "server.error");
        return pd;
    }
}

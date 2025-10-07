package cz.stavbau.backend.common.api;

import cz.stavbau.backend.common.exception.ConflictException;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Profile("dev")
@RestControllerAdvice
public class DevExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleAny(Throwable ex) {
        // Default 500
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Častý případ: porušení DB pravidel -> 400, ať FE ví, že je to chyba vstupu
        if (ex instanceof DataIntegrityViolationException) {
            status = HttpStatus.BAD_REQUEST;
        }

        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(status == HttpStatus.BAD_REQUEST ? "Data integrity violation" : "Server error (dev)");
        pd.setDetail(rootMessage(ex)); // <<< tady bude skutečná příčina (constraint/NULL/duplikát…)
        pd.setProperty("exception", ex.getClass().getName());
        return pd;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        // 409 – konflikt (např. duplicitní IČO v rámci company)
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflict");
        // V ex.getMessage() míváme náš kód (např. "customer.ico.exists")
        pd.setDetail(ex.getMessage());
        pd.setProperty("code", ex.getMessage());
        return pd;
    }

    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        String last = null;
        while (cur != null) { last = cur.getMessage(); cur = cur.getCause(); }
        return last;
    }
}
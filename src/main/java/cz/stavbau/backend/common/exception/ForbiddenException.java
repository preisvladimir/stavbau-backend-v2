package cz.stavbau.backend.common.exception;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Doménová 403 výjimka (neplést se Spring Security AccessDeniedException).
 * Použij ji tam, kde chceš výslovně vracet 403 a předat FE i requiredScopes.
 */
@Getter
public class ForbiddenException extends DomainException {

    private final List<String> requiredScopes;

    public ForbiddenException(String message) {
        super(message);
        this.requiredScopes = Collections.emptyList();
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
        this.requiredScopes = Collections.emptyList();
    }

    public ForbiddenException(String message, List<String> requiredScopes) {
        super(message);
        this.requiredScopes = requiredScopes == null ? Collections.emptyList() : List.copyOf(requiredScopes);
    }

    public ForbiddenException(String message, Throwable cause, List<String> requiredScopes) {
        super(message, cause);
        this.requiredScopes = requiredScopes == null ? Collections.emptyList() : List.copyOf(requiredScopes);
    }

    /** Pohodlná factory – nejčastější případ: chybí konkrétní scope(y). */
    public static ForbiddenException missingScopes(String... scopes) {
        return new ForbiddenException(
                "Missing required scopes",
                scopes == null ? Collections.emptyList() : Arrays.asList(scopes)
        );
    }
}

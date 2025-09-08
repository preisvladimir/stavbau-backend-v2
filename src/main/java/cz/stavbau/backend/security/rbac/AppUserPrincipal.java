package cz.stavbau.backend.security.rbac;

import java.util.UUID;

public class AppUserPrincipal {
    private final UUID userId;
    private final UUID companyId;
    private final String email;

    public AppUserPrincipal(UUID userId, UUID companyId, String email) {
        this.userId = userId;
        this.companyId = companyId;
        this.email = email;
    }

    public UUID getUserId() { return userId; }
    public UUID getCompanyId() { return companyId; }
    public String getEmail() { return email; }
}

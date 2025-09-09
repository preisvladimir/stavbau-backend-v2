package cz.stavbau.backend.security.jwt;

import cz.stavbau.backend.security.AppUserPrincipal;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.security.rbac.ProjectRoleAssignment;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.stereotype.Component;

import java.util.*;

import static cz.stavbau.backend.security.jwt.JwtService.*;

/**
 * Mapper Claim(s) → AppUserPrincipal.
 * Používá helpery z JwtService (extractCompanyRole / extractProjectRoles / extractScopes).
 */
@Component
public class ClaimsPrincipalMapper {

    private final JwtService jwtService;

    public ClaimsPrincipalMapper(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Konverze z ověřeného JWS na principal.
     * - subject (sub) = userId (UUID)
     * - cid (companyId) je volitelný
     * - email je volitelný
     * - RBAC claims volitelné
     */
    public AppUserPrincipal toPrincipal(Jws<Claims> jws) {
        Claims c = jws.getBody();

        UUID userId = parseUuidSafe(c.getSubject());
        UUID companyId = parseUuidSafe((String) c.get(CLAIM_COMPANY_ID));
        String email = (String) c.getOrDefault(CLAIM_EMAIL, null);

        CompanyRoleName companyRole = jwtService.extractCompanyRole(c);
        List<ProjectRoleAssignment> projectRoles = jwtService.extractProjectRoles(c);
        Set<String> scopes = jwtService.extractScopes(c);

        return new AppUserPrincipal(userId, companyId, email, companyRole, projectRoles, scopes);
    }

    private static UUID parseUuidSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try { return UUID.fromString(value); } catch (Exception ignored) { return null; }
    }
}

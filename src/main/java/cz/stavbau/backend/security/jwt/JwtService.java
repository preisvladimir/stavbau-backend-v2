package cz.stavbau.backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.security.rbac.ProjectRoleAssignment;
import cz.stavbau.backend.security.rbac.ProjectRoleName;

/**
 * JWT služba pro vydávání a validaci access/refresh tokenů.
 * Rozšířeno o RBAC claims:
 *  - companyRole: {@link CompanyRoleName}
 *  - projectRoles: pole objektů { projectId: UUID, role: enumName }
 *  - scopes: pole stringů "area:action"
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    /** Feature flag: zda přidávat RBAC claimy (companyRole, projectRoles, scopes). */
    private final boolean rbacClaimsEnabled;

    // === Claim keys (stabilní názvy pro FE/BE integraci) ===
    /** Company (tenant) UUID (string). */
    public static final String CLAIM_COMPANY_ID    = "cid";
    /** E-mail uživatele (pomůcka pro FE toggly/UI). */
    public static final String CLAIM_EMAIL         = "email";
    /** Company role (enum name, např. COMPANY_ADMIN). */
    public static final String CLAIM_COMPANY_ROLE  = "companyRole";
    /** Project roles (array of { projectId: UUID-string, role: enumName }). */
    public static final String CLAIM_PROJECT_ROLES = "projectRoles";
    /** Scopes (array of strings "area:action"). */
    public static final String CLAIM_SCOPES        = "scopes";


    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.issuer:stavbau}") String issuer,
            @Value("${app.security.jwt.accessTokenTtlMinutes:30}") long accessTtlMinutes,
            @Value("${app.security.jwt.refreshTokenTtlDays:14}") long refreshTtlDays,
            @Value("${app.security.jwt.rbacClaimsEnabled:true}") boolean rbacClaimsEnabled
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlMinutes * 60;
        this.refreshTtlSeconds = refreshTtlDays * 24 * 60 * 60;
        this.rbacClaimsEnabled = rbacClaimsEnabled;
    }

    public boolean isRbacClaimsEnabled() {
        return rbacClaimsEnabled;
    }

    /**
     * Původní (kompatibilní) vydání access tokenu – bez RBAC claimů.
     */
    public String issueAccessToken(UUID userId, UUID companyId, String email) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        if (companyId != null) {
            claims.put(CLAIM_COMPANY_ID, companyId.toString());
        }
        if (email != null) {
            claims.put(CLAIM_EMAIL, email);
        }
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId.toString())
                .setAudience("api")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * RBAC-ready vydání access tokenu – doplní companyRole, projectRoles a scopes.
     * Vstupy mohou být null/prázdné – do tokenu se pak daný claim neuloží.
     */
    public String issueAccessToken(UUID userId,
                                   UUID companyId,
                                   String email,
                                   CompanyRoleName companyRole,
                                   List<ProjectRoleAssignment> projectRoles,
                                   Set<String> scopes) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();

        if (companyId != null) {
            claims.put(CLAIM_COMPANY_ID, companyId.toString());
        }
        if (email != null) {
            claims.put(CLAIM_EMAIL, email);
        }
        if (companyRole != null) {
            claims.put(CLAIM_COMPANY_ROLE, companyRole.name());
        }
        if (projectRoles != null && !projectRoles.isEmpty()) {
            // Serializace do JSON-friendly struktury
            List<Map<String, Object>> serialized = projectRoles.stream()
                    .filter(Objects::nonNull)
                    .map(pra -> {
                        Map<String, Object> m = new HashMap<>();
                        if (pra.projectId() != null) {
                            m.put("projectId", pra.projectId().toString());
                        }
                        if (pra.role() != null) {
                            m.put("role", pra.role().name());
                        }
                        return m;
                    })
                    .collect(Collectors.toList());
            if (!serialized.isEmpty()) {
                claims.put(CLAIM_PROJECT_ROLES, serialized);
            }
        }
        if (scopes != null && !scopes.isEmpty()) {
            // Zachováme pořadí deterministicky
            List<String> list = scopes.stream()
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                claims.put(CLAIM_SCOPES, list);
            }
        }

        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId.toString())
                .setAudience("api")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Vytvoří refresh JWT s jti a tokenVersion – pro rotaci a revokaci. */
    public String issueRefreshToken(UUID userId, int tokenVersion, UUID refreshJti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId.toString())
                .setAudience("refresh")
                .setId(refreshJti.toString())                  // jti
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .addClaims(Map.of("ver", tokenVersion))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) throws JwtException {
        return Jwts.parserBuilder()
                .requireIssuer(issuer)
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    // ===== Helpery pro extrakci RBAC claimů z Claims =====

    /**
     * Načte company roli z claimů. Pokud chybí nebo je neznámá, vrací null.
     */
    public CompanyRoleName extractCompanyRole(Claims claims) {
        Object v = claims.get(CLAIM_COMPANY_ROLE);
        if (v instanceof String s && !s.isBlank()) {
            try {
                return CompanyRoleName.valueOf(s);
            } catch (IllegalArgumentException ignored) {
                // neznámá hodnota – vrátíme null
            }
        }
        return null;
    }

    /**
     * Načte projektové role z claimů. Očekává List<Map<String,Object>>
     * s klíči "projectId" (UUID string) a "role" (enum name).
     * Nevalidní položky jsou přeskočeny.
     */
    @SuppressWarnings("unchecked")
    public List<ProjectRoleAssignment> extractProjectRoles(Claims claims) {
        Object v = claims.get(CLAIM_PROJECT_ROLES);
        if (!(v instanceof List<?> rawList)) {
            return List.of();
        }
        List<ProjectRoleAssignment> result = new ArrayList<>();
        for (Object o : rawList) {
            if (o instanceof Map<?, ?> m) {
                Object pid = m.get("projectId");
                Object role = m.get("role");
                if (pid instanceof String pidStr && role instanceof String roleStr) {
                    try {
                        UUID projectId = UUID.fromString(pidStr);
                        ProjectRoleName roleName = ProjectRoleName.valueOf(roleStr);
                        result.add(new ProjectRoleAssignment(projectId, roleName));
                    } catch (Exception ignored) {
                        // přeskočit nevalidní záznam
                    }
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Načte scopes z claimů. Očekává List<?>; ne-string prvky ignoruje.
     */
    @SuppressWarnings("unchecked")
    public Set<String> extractScopes(Claims claims) {
        Object v = claims.get(CLAIM_SCOPES);
        if (v instanceof List<?> list) {
            Set<String> out = new LinkedHashSet<>();
            for (Object o : list) {
                if (o instanceof String s && !s.isBlank()) {
                    out.add(s);
                }
            }
            return Collections.unmodifiableSet(out);
        }
        // fallback – někteří klienti mohou poslat jediný scope jako string
        if (v instanceof String s && !s.isBlank()) {
            return Set.of(s);
        }
        return Set.of();
    }
}

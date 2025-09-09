package cz.stavbau.backend.security.jwt;

import cz.stavbau.backend.security.AppUserPrincipal;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT filter (Bearer) – validuje access token, promapuje Claims -> AppUserPrincipal
 * a nastaví Authentication do SecurityContextu.
 * Řetězení: RateLimitFilter -> JwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ClaimsPrincipalMapper claimsPrincipalMapper;

    public JwtAuthenticationFilter(JwtService jwtService, ClaimsPrincipalMapper claimsPrincipalMapper) {
        this.jwtService = jwtService;
        this.claimsPrincipalMapper = claimsPrincipalMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String token = resolveBearerToken(req);
        if (token != null) {
            try {
                Jws<Claims> jws = jwtService.parseAndValidate(token);

                // Mapování na náš principal (userId, companyId, email, companyRole, projectRoles, scopes)
                AppUserPrincipal principal = claimsPrincipalMapper.toPrincipal(jws);

                // Authorities (volitelné): ROLE_USER + ROLE_{CompanyRole} + SCOPE_{scope}
                var authorities = buildAuthorities(principal);

                var authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (JwtException e) {
                // Neplatný token → nepřihlásíme; downstream skončí 401/403 dle Security configu.
            } catch (Exception e) {
                // Defenzivně – cokoliv jiného nesmí shodit request; pokračuj bez autentizace.
            }
        }

        chain.doFilter(req, res);
    }

    private static String resolveBearerToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    private static List<SimpleGrantedAuthority> buildAuthorities(AppUserPrincipal p) {
        List<SimpleGrantedAuthority> out = new ArrayList<>();
        // Základní role pro případné globální guardy
        out.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Company role -> ROLE_{ENUM}
        CompanyRoleName cr = p.getCompanyRole();
        if (cr != null) {
            out.add(new SimpleGrantedAuthority("ROLE_" + cr.name()));
        }

        // Scopes -> SCOPE_{area:action}
        if (p.getScopes() != null && !p.getScopes().isEmpty()) {
            out.addAll(p.getScopes().stream()
                    .filter(Objects::nonNull)
                    .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                    .collect(Collectors.toList()));
        }
        return out;
    }
}

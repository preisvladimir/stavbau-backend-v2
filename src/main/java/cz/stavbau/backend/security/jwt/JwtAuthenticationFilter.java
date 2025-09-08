package cz.stavbau.backend.security.jwt;

import cz.stavbau.backend.security.rbac.AppUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtAuthenticationFilter(JwtService jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var jws = jwt.parseAndValidate(token);
                Claims c = jws.getBody();
                UUID userId = UUID.fromString(c.getSubject());
                UUID companyId = Optional.ofNullable((String)c.get("cid")).map(UUID::fromString).orElse(null);
                String email = (String) c.get("email");
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                var principal = new cz.stavbau.backend.security.rbac.AppUserPrincipal(userId, companyId, email);
                var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (JwtException e) {
                // invalid access token → pokračuj bez autentizace (skončí 401/403 dle configu)
            }
        }
        chain.doFilter(req, res);
    }
}

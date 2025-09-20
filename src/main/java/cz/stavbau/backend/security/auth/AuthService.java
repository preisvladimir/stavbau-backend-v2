package cz.stavbau.backend.security.auth;

import cz.stavbau.backend.security.auth.dto.AuthResponse;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import cz.stavbau.backend.tenants.membership.repo.CompanyMemberRepository;
import cz.stavbau.backend.security.jwt.JwtService;
import cz.stavbau.backend.security.rbac.BuiltInRoles;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.users.repo.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       CompanyMemberRepository companyMemberRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(String email, String rawPassword) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("auth.invalid_credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("auth.invalid_credentials");
        }

        // ⬇️ bezpečné IDčka
        UUID userId = user.getId();
        UUID companyId = user.getCompanyId(); // nebo odkud čteš aktivní firmu
        if (userId == null) throw new IllegalStateException("User ID is null");
        // companyId může být null (pak se do JWT nepřidá)

        // ⬇️ bezpečné načtení role + fallback
        CompanyRoleName role = companyMemberRepository
                .findByUserIdAndCompanyId(userId, companyId)
                .map(CompanyMember::getRole)
                .orElse(CompanyRoleName.VIEWER);

        Set<String> scopes = BuiltInRoles.COMPANY_ROLE_SCOPES
                .getOrDefault(role, Set.of());

        // ⬇️ RBAC overload (tvůj JwtService)
        String accessToken = jwtService.issueAccessToken(
                userId,
                companyId,
                user.getEmail(),
                role,
                List.of(),   // projectRoles – Sprint 3
                scopes
        );

        String refreshToken = jwtService.issueRefreshToken(
                userId,
                user.getTokenVersion(),
                UUID.randomUUID()
        );

        return new AuthResponse(accessToken, refreshToken);
    }



}

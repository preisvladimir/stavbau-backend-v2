package cz.stavbau.backend.security;
import org.springframework.security.core.*; import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

public final class SecurityUtils {
    private SecurityUtils(){}
    public static UUID currentUserId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal()==null) return null;
        if (auth.getPrincipal() instanceof AppUserPrincipal p) return p.getUserId();
        return null;
    }
    public static UUID currentCompanyId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal()==null) return null;
        if (auth.getPrincipal() instanceof AppUserPrincipal p) return p.getCompanyId();
        return null;
    }
}

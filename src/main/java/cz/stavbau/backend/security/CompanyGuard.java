package cz.stavbau.backend.security;

import cz.stavbau.backend.common.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("companyGuard")
public class CompanyGuard {
    public boolean sameCompany(UUID companyId, AppUserPrincipal principal) {
        return principal != null && companyId != null && companyId.equals(principal.getCompanyId());
    }
    public void assertCompany(UUID companyId, AppUserPrincipal principal) {
        if (!sameCompany(companyId, principal)) {
            throw new ForbiddenException("errors.forbidden.company.mismatch");
        }
    }
}
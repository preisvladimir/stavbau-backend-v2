// src/main/java/cz/stavbau/backend/common/domain/CompanyScoped.java
package cz.stavbau.backend.common.domain;

import java.util.UUID;

/**
 * Marker interface pro entity/DTO, které jsou vázané na firmu (tenant).
 * Implementací tohoto rozhraní dává entita jasně najevo,
 * že patří do konkrétní firmy (companyId).
 *
 * Používáme např. pro guardy v servisu:
 *   if (!entity.getCompanyId().equals(SecurityUtils.currentCompanyId())) throw new ForbiddenException(...);
 */
public interface CompanyScoped {

    /**
     * Vrací ID firmy (tenant), ke které entita náleží.
     */
    UUID getCompanyId();

    /**
     * Nastaví ID firmy (tenant).
     */
    void setCompanyId(UUID companyId);
}

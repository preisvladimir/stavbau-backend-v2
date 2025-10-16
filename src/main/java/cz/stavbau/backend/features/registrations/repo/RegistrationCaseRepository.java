package cz.stavbau.backend.features.registrations.repo;

import cz.stavbau.backend.features.registrations.model.RegistrationCase;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

@Repository
public interface RegistrationCaseRepository extends JpaRepository<RegistrationCase, UUID> {

    // Aktivní registrace pro e-mail (status IN EMAIL_SENT, EMAIL_VERIFIED, APPROVED)
    @Query("""
           SELECT rc FROM RegistrationCase rc
           WHERE rc.email = :email
             AND rc.status IN ('EMAIL_SENT','EMAIL_VERIFIED','APPROVED')
           """)
    List<RegistrationCase> findActiveByEmail(@Param("email") String email);

    // Podle token hash (pro confirm)
    Optional<RegistrationCase> findByTokenHash(String tokenHash);

    // Lock podle token hash (pro confirm, jednorázovost + race)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT rc FROM RegistrationCase rc
           WHERE rc.tokenHash = :tokenHash
           """)
    Optional<RegistrationCase> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    // Lock podle id (pro resend/send-confirm)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT rc FROM RegistrationCase rc
           WHERE rc.id = :id
           """)
    Optional<RegistrationCase> findByIdForUpdate(@Param("id") UUID id);

    // Pro expirační job
    List<RegistrationCase> findAllByExpiresAtBefore(Instant now);

    /**
     * Vybere dávku "aktivních" registrací s uplynulým expires_at, uzamkne je
     * a vrátí k expiračnímu zpracování. Používá nativní SQL kvůli
     * FOR UPDATE SKIP LOCKED + LIMIT.
     */
    @Query(value = """
    SELECT *
    FROM registration_cases
    WHERE expires_at < :now
      AND status IN ('EMAIL_SENT','EMAIL_VERIFIED','APPROVED')
    FOR UPDATE SKIP LOCKED
    LIMIT :batchSize
    """, nativeQuery = true)
    List<RegistrationCase> lockBatchForExpiration(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize);
}

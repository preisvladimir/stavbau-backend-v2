package cz.stavbau.backend.invoices.repo;

import cz.stavbau.backend.invoices.model.NumberSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface NumberSeriesRepository extends JpaRepository<NumberSeries, UUID> {

    Optional<NumberSeries> findByCompanyIdAndKeyAndCounterYear(UUID companyId, String key, int counterYear);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ns from NumberSeries ns where ns.companyId = :companyId and ns.key = :key and ns.counterYear = :year")
    Optional<NumberSeries> lockByCompanyKeyYear(@Param("companyId") UUID companyId,
                                                @Param("key") String key,
                                                @Param("year") int year);

    Optional<NumberSeries> findFirstByCompanyIdAndDefaultSeriesTrueOrderByCounterYearDesc(UUID companyId);
}

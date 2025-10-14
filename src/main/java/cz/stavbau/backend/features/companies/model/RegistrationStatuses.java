package cz.stavbau.backend.features.companies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Stav registrací dle ARES (VR/RES/RŽP/DPH/…).
 * Ukládáme textové hodnoty "AKTIVNI" / "NEEXISTUJICI" apod.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Embeddable
public class RegistrationStatuses {
    @Column(length = 20) private String stavZdrojeVr;
    @Column(length = 20) private String stavZdrojeRes;
    @Column(length = 20) private String stavZdrojeRzp;
    @Column(length = 20) private String stavZdrojeNrpzs;
    @Column(length = 20) private String stavZdrojeRpsh;
    @Column(length = 20) private String stavZdrojeRcns;
    @Column(length = 20) private String stavZdrojeSzr;
    @Column(length = 20) private String stavZdrojeDph;
    @Column(length = 20) private String stavZdrojeSd;
    @Column(length = 20) private String stavZdrojeIr;
    @Column(length = 20) private String stavZdrojeCeu;
    @Column(length = 20) private String stavZdrojeRs;
    @Column(length = 20) private String stavZdrojeRed;
    @Column(length = 20) private String stavZdrojeMonitor;
}

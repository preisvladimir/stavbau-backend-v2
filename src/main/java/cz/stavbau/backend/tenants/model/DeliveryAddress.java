package cz.stavbau.backend.tenants.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Embeddable
public class DeliveryAddress {
    @Column(length = 255) private String radekAdresy1;
    @Column(length = 255) private String radekAdresy2;
}

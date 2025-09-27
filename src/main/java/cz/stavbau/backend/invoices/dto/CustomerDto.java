// src/main/java/cz/stavbau/backend/invoices/dto/CustomerDto.java
package cz.stavbau.backend.invoices.dto;

import cz.stavbau.backend.common.api.dto.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto{
        private UUID id;
        private  UUID companyId;
        private  String type;
        private  String name;
        private String ico;
        private String dic;
        private  String email;
        private String phone;
        // nový typed objekt
        private AddressDto billingAddress;
        // dočasný legacy string
        @Schema(deprecated = true, description = "DEPRECATED – bude odstraněno po přechodu FE")
        private String billingAddressJson;
        private Integer defaultPaymentTermsDays;
        private String notes;
        private  UUID linkedUserId;
}

// src/main/java/cz/stavbau/backend/invoices/dto/CustomerDto.java
package cz.stavbau.backend.customers.dto;

import cz.stavbau.backend.common.api.dto.AddressDto;
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
        private AddressDto billingAddress;
        private Integer defaultPaymentTermsDays;
        private String notes;
        private  UUID linkedUserId;
}

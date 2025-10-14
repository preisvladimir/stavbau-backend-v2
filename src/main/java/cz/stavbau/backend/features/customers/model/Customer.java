// src/main/java/cz/stavbau/backend/invoices/model/Customer.java
package cz.stavbau.backend.features.customers.model;

import cz.stavbau.backend.common.domain.Address;
import cz.stavbau.backend.common.domain.BaseEntity;
import cz.stavbau.backend.common.domain.CompanyScoped; // <— správný import
import cz.stavbau.backend.common.persistence.AddressJsonConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity implements CompanyScoped {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "type", nullable = false, length = 16)
    private String type = "ORGANIZATION"; // PERSON|ORGANIZATION (MVP: string enum)

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "ico", length = 32)
    private String ico;

    @Column(name = "dic", length = 32)
    private String dic;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 64)
    private String phone;

    @Convert(converter = AddressJsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)                    // ⬅️ klíčové pro správné bindování
    @Column(name = "billing_address", columnDefinition = "jsonb")
    private Address billingAddress;

    @Column(name = "default_payment_terms_days")
    private Integer defaultPaymentTermsDays;

    @Column(name = "notes")
    private String notes;

    @Column(name = "linked_user_id")
    private UUID linkedUserId;
}

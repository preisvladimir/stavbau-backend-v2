package cz.stavbau.backend.tenants.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import jakarta.persistence.*; import lombok.*;

@Entity @Table(name="companies") @Getter @Setter
public class Company extends BaseEntity {
    @Column(nullable=false)
    private String name;
    @Column(name="default_locale", nullable=false)
    private String defaultLocale = "cs";

    public String getName() {
        return name;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}

package cz.stavbau.backend.features.files.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_tags",
       uniqueConstraints = @UniqueConstraint(name="ux_file_tags_company_name", columnNames = {"company_id","name"}))
public class FileTag {
    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

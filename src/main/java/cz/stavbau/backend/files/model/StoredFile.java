package cz.stavbau.backend.files.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "stored_files")
public class StoredFile {
    @Id
    private UUID id;
    @Column(nullable=false) private UUID company_id;
    @Column(nullable=false) private UUID uploader_id;
    private String original_name;
    private String mime_type;
    private long size_bytes;
    private String sha256;
    private String storage_key;
    private Instant created_at;
}

package cz.stavbau.backend.files.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_links")
public class FileLink {
    @EmbeddedId
    private FileLinkId id = new FileLinkId();

    public FileLink() {}
    public FileLink(UUID fileId, LinkTarget targetType, UUID targetId) {
        this.id.fileId = fileId;
        this.id.targetType = targetType;
        this.id.targetId = targetId;
    }

    public FileLinkId getId() { return id; }
    public void setId(FileLinkId id) { this.id = id; }

    @Embeddable
    public static class FileLinkId {
        @Column(name="file_id") public UUID fileId;
        @Enumerated(EnumType.STRING) @Column(name="target_type") public LinkTarget targetType;
        @Column(name="target_id") public UUID targetId;
    }
}

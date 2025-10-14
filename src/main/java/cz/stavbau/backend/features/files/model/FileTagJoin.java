package cz.stavbau.backend.features.files.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_tag_join")
public class FileTagJoin {
    @EmbeddedId
    private FileTagJoinId id = new FileTagJoinId();

    public FileTagJoin() {}
    public FileTagJoin(UUID fileId, UUID tagId) {
        this.id.fileId = fileId;
        this.id.tagId = tagId;
    }

    public FileTagJoinId getId() { return id; }
    public void setId(FileTagJoinId id) { this.id = id; }

    @Embeddable
    public static class FileTagJoinId {
        @Column(name="file_id") public UUID fileId;
        @Column(name="tag_id") public UUID tagId;
    }
}

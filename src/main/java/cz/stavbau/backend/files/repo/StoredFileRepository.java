package cz.stavbau.backend.files.repo;
import cz.stavbau.backend.files.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {}

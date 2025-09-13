package cz.stavbau.backend.files.repo;

import cz.stavbau.backend.files.model.FileTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileTagRepository extends JpaRepository<FileTag, UUID> {
    Optional<FileTag> findByCompanyIdAndName(UUID companyId, String name);
}

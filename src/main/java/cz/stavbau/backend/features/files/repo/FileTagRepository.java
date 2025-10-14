package cz.stavbau.backend.features.files.repo;

import cz.stavbau.backend.features.files.model.FileTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileTagRepository extends JpaRepository<FileTag, UUID> {
    Optional<FileTag> findByCompanyIdAndName(UUID companyId, String name);
}

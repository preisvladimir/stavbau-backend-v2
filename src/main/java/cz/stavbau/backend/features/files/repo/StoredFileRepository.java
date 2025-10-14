package cz.stavbau.backend.features.files.repo;

import cz.stavbau.backend.features.files.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {}

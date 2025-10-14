package cz.stavbau.backend.features.files.repo;

import cz.stavbau.backend.features.files.model.FileLink;
import cz.stavbau.backend.features.files.model.FileLink.FileLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileLinkRepository extends JpaRepository<FileLink, FileLinkId> {}

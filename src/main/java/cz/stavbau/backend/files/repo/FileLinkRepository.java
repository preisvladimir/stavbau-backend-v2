package cz.stavbau.backend.files.repo;

import cz.stavbau.backend.files.model.FileLink;
import cz.stavbau.backend.files.model.FileLink.FileLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileLinkRepository extends JpaRepository<FileLink, FileLinkId> {}

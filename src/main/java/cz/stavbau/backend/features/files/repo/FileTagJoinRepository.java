package cz.stavbau.backend.features.files.repo;

import cz.stavbau.backend.features.files.model.FileTagJoin;
import cz.stavbau.backend.features.files.model.FileTagJoin.FileTagJoinId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileTagJoinRepository extends JpaRepository<FileTagJoin, FileTagJoinId> {}

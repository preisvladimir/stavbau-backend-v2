package cz.stavbau.backend.files.repo;

import cz.stavbau.backend.files.model.FileTagJoin;
import cz.stavbau.backend.files.model.FileTagJoin.FileTagJoinId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileTagJoinRepository extends JpaRepository<FileTagJoin, FileTagJoinId> {}

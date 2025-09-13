package cz.stavbau.backend.files.config;

import cz.stavbau.backend.files.storage.FileStorage;
import cz.stavbau.backend.files.storage.LocalFileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class FilesConfiguration {

    @Bean
    public FileStorage fileStorage(
            @Value("${files.storage.local.base-path:/mnt/data/stavbau-data/files}") String basePath
    ) {
        return new LocalFileStorage(Path.of(basePath));
    }
}

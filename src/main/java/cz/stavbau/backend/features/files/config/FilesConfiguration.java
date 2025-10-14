package cz.stavbau.backend.features.files.config;

import cz.stavbau.backend.features.files.storage.FileStorage;
import cz.stavbau.backend.features.files.storage.LocalFileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class FilesConfiguration {

    @Bean
    public FileStorage fileStorage(
            @Value("${files.storage.local.sessions-path:/mnt/data/stavbau-data/files}") String basePath
    ) {
        return new LocalFileStorage(Path.of(basePath));
    }
}

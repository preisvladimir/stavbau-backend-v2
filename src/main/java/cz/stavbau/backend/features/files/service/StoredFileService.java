package cz.stavbau.backend.features.files.service;

import cz.stavbau.backend.features.files.model.LinkTarget;
import cz.stavbau.backend.features.files.model.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StoredFileService {
    StoredFile upload(UUID companyId, UUID uploaderId, MultipartFile file);

    byte[] download(UUID fileId);

    void delete(UUID fileId);

    void setTags(UUID fileId, List<String> tags);

    void link(UUID fileId, LinkTarget targetType, UUID targetId);
}

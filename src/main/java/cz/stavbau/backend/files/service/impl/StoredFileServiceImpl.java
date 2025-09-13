package cz.stavbau.backend.files.service.impl;

import cz.stavbau.backend.files.model.*;
import cz.stavbau.backend.files.repo.*;
import cz.stavbau.backend.files.service.StoredFileService;
import cz.stavbau.backend.files.storage.FileStorage;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class StoredFileServiceImpl implements StoredFileService {

    private final StoredFileRepository fileRepo;
    private final FileTagRepository tagRepo;
    private final FileTagJoinRepository tagJoinRepo;
    private final FileLinkRepository linkRepo;
    private final FileStorage storage;

    public StoredFileServiceImpl(StoredFileRepository fileRepo,
                                 FileTagRepository tagRepo,
                                 FileTagJoinRepository tagJoinRepo,
                                 FileLinkRepository linkRepo,
                                 FileStorage storage) {
        this.fileRepo = fileRepo;
        this.tagRepo = tagRepo;
        this.tagJoinRepo = tagJoinRepo;
        this.linkRepo = linkRepo;
        this.storage = storage;
    }

    @Override
    @Transactional
    public StoredFile upload(UUID companyId, UUID uploaderId, MultipartFile file) {
        try {
            var res = storage.store(companyId.toString(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream());

            StoredFile sf = new StoredFile();
            sf.setId(UUID.randomUUID());
            sf.setCompanyId(companyId);
            sf.setUploaderId(uploaderId);
            sf.setOriginalName(file.getOriginalFilename());
            sf.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            sf.setSizeBytes(file.getSize());
            sf.setSha256(res.sha256());
            sf.setStorageKey(res.storageKey());
            sf.setCreatedAt(Instant.now());
            return fileRepo.save(sf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] download(UUID fileId) {
        StoredFile sf = fileRepo.findById(fileId).orElseThrow();
        try (var is = storage.read(sf.getStorageKey())) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void delete(UUID fileId) {
        StoredFile sf = fileRepo.findById(fileId).orElseThrow();
        storage.delete(sf.getStorageKey());
        fileRepo.delete(sf);
    }

    @Override
    @Transactional
    public void setTags(UUID fileId, List<String> tags) {
        StoredFile sf = fileRepo.findById(fileId).orElseThrow();
        UUID companyId = sf.getCompanyId();

        // remove existing joins (simple approach: delete all and re-add)
        // could be optimized by diffing
        tagJoinRepo.deleteAll(
            tagJoinRepo.findAll().stream().filter(j -> j.getId().fileId.equals(fileId)).toList()
        );

        for (String name : tags) {
            var tag = tagRepo.findByCompanyIdAndName(companyId, name)
                    .orElseGet(() -> {
                        FileTag t = new FileTag();
                        t.setId(UUID.randomUUID());
                        t.setCompanyId(companyId);
                        t.setName(name);
                        return tagRepo.save(t);
                    });
            tagJoinRepo.save(new FileTagJoin(fileId, tag.getId()));
        }
    }

    @Override
    @Transactional
    public void link(UUID fileId, LinkTarget targetType, UUID targetId) {
        linkRepo.save(new FileLink(fileId, targetType, targetId));
    }
}

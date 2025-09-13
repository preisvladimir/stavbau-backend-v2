package cz.stavbau.backend.files.storage;

import java.io.InputStream;

public interface FileStorage {
    record StoreResult(String storageKey, String sha256) {}
    StoreResult store(String companyPrefix, String originalFilename, String mimeType, long size, InputStream data);
    InputStream read(String storageKey);
    void delete(String storageKey);
}

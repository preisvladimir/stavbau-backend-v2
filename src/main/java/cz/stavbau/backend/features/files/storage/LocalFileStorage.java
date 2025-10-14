package cz.stavbau.backend.features.files.storage;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public class LocalFileStorage implements FileStorage {
    private final Path basePath;

    public LocalFileStorage(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public StoreResult store(String companyPrefix, String originalFilename, String mimeType, long size, InputStream data) {
        try {
            String ext = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
            String fname = UUID.randomUUID() + ext;
            Path dir = basePath.resolve(companyPrefix);
            Files.createDirectories(dir);
            Path target = dir.resolve(fname);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (OutputStream os = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                data.transferTo(new DigestOutputStream(os, md));
            }
            String sha = HexFormat.of().formatHex(md.digest());
            return new StoreResult(companyPrefix + "/" + fname, sha);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class DigestOutputStream extends FilterOutputStream {
        private final MessageDigest md;
        public DigestOutputStream(OutputStream out, MessageDigest md) { super(out); this.md = md; }
        @Override public void write(int b) throws IOException { md.update((byte)b); out.write(b); }
        @Override public void write(byte[] b, int off, int len) throws IOException { md.update(b, off, len); out.write(b, off, len); }
    }

    @Override public InputStream read(String storageKey) {
        try { return Files.newInputStream(basePath.resolve(storageKey)); }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    @Override public void delete(String storageKey) {
        try { Files.deleteIfExists(basePath.resolve(storageKey)); }
        catch (IOException e) { throw new RuntimeException(e); }
    }
}

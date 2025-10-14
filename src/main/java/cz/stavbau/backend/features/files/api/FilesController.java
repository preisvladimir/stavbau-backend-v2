package cz.stavbau.backend.features.files.api;

import cz.stavbau.backend.features.files.api.dto.LinkRequest;
import cz.stavbau.backend.features.files.api.dto.StoredFileResponse;
import cz.stavbau.backend.features.files.api.dto.TagsRequest;
import cz.stavbau.backend.features.files.model.StoredFile;
import cz.stavbau.backend.features.files.service.StoredFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "Upload, download, tagy a linky")
public class FilesController {

    private final StoredFileService storedFileService;

    public FilesController(StoredFileService storedFileService) {
        this.storedFileService = storedFileService;
    }

    private static StoredFileResponse map(StoredFile f) {
        return new StoredFileResponse(f.getId(), f.getCompanyId(), f.getUploaderId(),
                f.getOriginalName(), f.getMimeType(), f.getSizeBytes(), f.getSha256(), f.getStorageKey(), f.getCreatedAt());
    }

    @Operation(summary = "Upload souboru")
    @PreAuthorize("hasAuthority('files:write')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoredFileResponse> upload(
            @RequestParam UUID companyId,
            @RequestParam UUID uploaderId,
            @RequestPart("file") MultipartFile file
    ) {
        var saved = storedFileService.upload(companyId, uploaderId, file);
        return ResponseEntity.ok(map(saved));
    }

    @Operation(summary = "Stažení souboru")
    @PreAuthorize("hasAuthority('files:read')")
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable UUID id,
                                           @RequestParam(defaultValue = "attachment") String disposition) {
        byte[] bytes = storedFileService.download(id);
        // filename placeholder; in a real impl fetch metadata
        String fileName = id.toString();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(bytes.length);
        headers.setContentDisposition(ContentDisposition.parse(disposition + "; filename*=UTF-8''" +
                URLEncoder.encode(fileName, StandardCharsets.UTF_8)));
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "Smazání souboru")
    @PreAuthorize("hasAuthority('files:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        storedFileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Nastaví tagy k souboru (nahradí)")
    @PreAuthorize("hasAuthority('files:tag')")
    @PostMapping("/{id}/tags")
    public ResponseEntity<Void> setTags(@PathVariable UUID id, @Valid @RequestBody TagsRequest req) {
        storedFileService.setTags(id, req.tags());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Propojí soubor s entitou (Company/Project/Invoice)")
    @PreAuthorize("hasAuthority('files:write')")
    @PostMapping("/{id}/link")
    public ResponseEntity<Void> link(@PathVariable UUID id, @Valid @RequestBody LinkRequest req) {
        storedFileService.link(id, req.targetType(), req.targetId());
        return ResponseEntity.noContent().build();
    }
}

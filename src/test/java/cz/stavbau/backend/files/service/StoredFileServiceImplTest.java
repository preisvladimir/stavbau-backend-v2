package cz.stavbau.backend.files.service;

import cz.stavbau.backend.files.model.*;
import cz.stavbau.backend.files.repo.*;
import cz.stavbau.backend.files.service.impl.StoredFileServiceImpl;
import cz.stavbau.backend.files.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class StoredFileServiceImplTest {

    private StoredFileRepository fileRepo;
    private FileTagRepository tagRepo;
    private FileTagJoinRepository tagJoinRepo;
    private FileLinkRepository linkRepo;
    private FileStorage storage;
    private StoredFileService service;

    @BeforeEach
    void setup() {
        fileRepo = Mockito.mock(StoredFileRepository.class);
        tagRepo = Mockito.mock(FileTagRepository.class);
        tagJoinRepo = Mockito.mock(FileTagJoinRepository.class);
        linkRepo = Mockito.mock(FileLinkRepository.class);
        storage = Mockito.mock(FileStorage.class);
        service = new StoredFileServiceImpl(fileRepo, tagRepo, tagJoinRepo, linkRepo, storage);
    }

    @Test
    void upload_saves_metadata_from_storage_result() {
        UUID company = UUID.randomUUID();
        UUID uploader = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file","test.txt","text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));

        when(storage.store(anyString(), anyString(), anyString(), anyLong(), any())).thenReturn(new FileStorage.StoreResult("c/uuid.txt","abc123"));
        when(fileRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        var saved = service.upload(company, uploader, file);
        assertNotNull(saved.getId());
        assertEquals("c/uuid.txt", saved.getStorageKey());
        assertEquals("abc123", saved.getSha256());
        assertEquals("test.txt", saved.getOriginalName());
        assertEquals("text/plain", saved.getMimeType());
    }

    @Test
    void setTags_creates_missing_tags_and_joins() {
        UUID fileId = UUID.randomUUID();
        StoredFile sf = new StoredFile();
        sf.setId(fileId);
        sf.setCompanyId(UUID.randomUUID());
        when(fileRepo.findById(eq(fileId))).thenReturn(Optional.of(sf));
        when(tagRepo.findByCompanyIdAndName(any(), any())).thenReturn(Optional.empty());
        when(tagRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        service.setTags(fileId, List.of("invoice","important"));
        verify(tagRepo, times(2)).save(any());
        verify(tagJoinRepo, times(2)).save(any());
    }

    @Test
    void link_saves_file_link() {
        UUID fileId = UUID.randomUUID();
        service.link(fileId, LinkTarget.INVOICE, UUID.randomUUID());
        verify(linkRepo, times(1)).save(any());
    }
}

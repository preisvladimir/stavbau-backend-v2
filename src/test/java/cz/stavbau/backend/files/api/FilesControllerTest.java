package cz.stavbau.backend.files.api;

import cz.stavbau.backend.features.files.api.FilesController;
import cz.stavbau.backend.features.files.api.dto.LinkRequest;
import cz.stavbau.backend.features.files.api.dto.TagsRequest;
import cz.stavbau.backend.features.files.model.LinkTarget;
import cz.stavbau.backend.features.files.model.StoredFile;
import cz.stavbau.backend.features.files.service.StoredFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FilesController.class)
class FilesControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean StoredFileService storedFileService;

    @Test
    void upload_returns_metadata() throws Exception {
        UUID company = UUID.randomUUID(), uploader = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file","a.txt","text/plain","hi".getBytes(StandardCharsets.UTF_8));
        StoredFile sf = new StoredFile();
        sf.setId(UUID.randomUUID()); sf.setCompanyId(company); sf.setUploaderId(uploader);
        sf.setOriginalName("a.txt"); sf.setMimeType("text/plain"); sf.setSizeBytes(2L);
        sf.setSha256("x"); sf.setStorageKey("c/uuid.txt");

        Mockito.when(storedFileService.upload(Mockito.eq(company), Mockito.eq(uploader), Mockito.any())).thenReturn(sf);

        mvc.perform(multipart("/api/v1/files")
                .file(file)
                .param("companyId", company.toString())
                .param("uploaderId", uploader.toString()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(sf.getId().toString())))
           .andExpect(jsonPath("$.originalName", is("a.txt")));
    }

    @Test
    void set_tags_204() throws Exception {
        UUID id = UUID.randomUUID();
        var body = new TagsRequest(java.util.List.of("x","y"));
        mvc.perform(post("/api/v1/files/" + id + "/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
           .andExpect(status().isNoContent());
    }

    @Test
    void link_204() throws Exception {
        UUID id = UUID.randomUUID();
        var body = new LinkRequest(LinkTarget.INVOICE, UUID.randomUUID());
        mvc.perform(post("/api/v1/files/" + id + "/link")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
           .andExpect(status().isNoContent());
    }
}

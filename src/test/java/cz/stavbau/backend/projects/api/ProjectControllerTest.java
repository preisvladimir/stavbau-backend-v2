// src/test/java/cz/stavbau/backend/projects/web/ProjectControllerTest.java
package cz.stavbau.backend.projects.api;

import cz.stavbau.backend.common.api.PageResponse;
import cz.stavbau.backend.projects.dto.ProjectSummaryDto;
import cz.stavbau.backend.projects.service.ProjectService;
import cz.stavbau.backend.security.rbac.RbacService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired MockMvc mvc;

    @MockBean ProjectService projectService;

    // @PreAuthorize používá @rbac.hasScope(...): mockneme, ať se evaluuje na true
    @MockBean RbacService rbac;

    @Test
    @WithMockUser
    void list_returnsI18nHeaders_and_usesAliasSorting() throws Exception {
        Mockito.when(rbac.hasScope(anyString())).thenReturn(true);

        var dto = new ProjectSummaryDto(/* id */java.util.UUID.randomUUID(), /* name */"Alfa", /* createdAt */Instant.parse("2025-01-01T00:00:00Z"));
        Page<ProjectSummaryDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        Mockito.when(projectService.list(anyString(), any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/v1/projects")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "name,asc")
                        .header("Accept-Language", "cs"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_LANGUAGE, org.hamcrest.Matchers.startsWith("cs")))
                .andExpect(header().string(HttpHeaders.VARY, org.hamcrest.Matchers.containsString("Accept-Language")))
                .andExpect(jsonPath("$.items[0].name").value("Alfa"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        // Zachytíme Pageable a ověříme, že sort alias byl přeložen na "translations.name" + stabilizační "id,DESC"
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(projectService).list(anyString(), captor.capture());

        var sort = captor.getValue().getSort();
        assertThat(sort.getOrderFor("translations.name")).isNotNull();
        assertThat(sort.getOrderFor("translations.name").isAscending()).isTrue();
        assertThat(sort.getOrderFor("id")).isNotNull();
        assertThat(sort.getOrderFor("id").isDescending()).isTrue();
    }
}

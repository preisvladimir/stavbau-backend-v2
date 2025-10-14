package cz.stavbau.backend.tenants.api;

import cz.stavbau.backend.features.companies.dto.CompanyRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyRegistrationControllerIT {
    //@org.springframework.boot.test.mock.mockito.MockBean
   // org.springframework.data.domain.AuditorAware<java.util.UUID> auditorAware;

    @Autowired MockMvc mvc;
    @Autowired com.fasterxml.jackson.databind.ObjectMapper om;

    @MockBean
    AuditorAware<UUID> auditorAware; // vrací prázdné Optional -> žádný NPE
    @Test
    void register_created_owner() throws Exception {
        var req = new CompanyRegistrationRequest(
                new CompanyRegistrationRequest.CompanyDto("12345678", null, "Test s.r.o.",
                        new CompanyRegistrationRequest.AddressDto("Ulice 1", "Praha", "11000", "CZ"), null),
                new CompanyRegistrationRequest.OwnerDto("owner@example.com", "SuperSecret123!", null, null, null),
                new CompanyRegistrationRequest.ConsentsDto(true, null)
        );

        mvc.perform(post("/api/v1/tenants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "cs")
                        .content(om.writeValueAsBytes(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerRole").value("OWNER"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.companyId").exists())
                .andExpect(jsonPath("$.ownerUserId").exists());
    }
}

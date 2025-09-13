package cz.stavbau.backend.invoices.api;

import cz.stavbau.backend.invoices.api.dto.InvoiceCreateRequest;
import cz.stavbau.backend.invoices.api.dto.InvoiceLinesUpsertRequest;
import cz.stavbau.backend.invoices.api.dto.InvoiceLineDto;
import cz.stavbau.backend.invoices.api.dto.InvoiceStatusChangeRequest;
import cz.stavbau.backend.invoices.model.InvoiceStatus;
import cz.stavbau.backend.invoices.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = InvoiceController.class)
class InvoiceControllerTest {

    @Autowired ObjectMapper om;

    @Autowired
    private MockMvc mvc;

    // aby se uspokojily případné závislosti filtrů
    @MockBean
    private cz.stavbau.backend.security.jwt.JwtService jwtService;

    @MockBean
    private cz.stavbau.backend.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean InvoiceService invoiceService;

    @Test
    void create_draft_returns_id() throws Exception {
        UUID id = UUID.randomUUID();

        // namockujeme createDraft "široce", aby nevadily odchylky parametrů
        BDDMockito.given(invoiceService.createDraft(
                any(), any(), any(), any(), any(), any(), anyString(), anyString()
        )).willReturn(id);

        var req = new InvoiceCreateRequest(
                UUID.randomUUID(), null,
                LocalDate.of(2025,9,12), LocalDate.of(2025,9,30), null,
                "CZK", "{}", "{}"
        );

        mvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void issue_returns_number() throws Exception {
        UUID id = UUID.randomUUID();

        BDDMockito.given(invoiceService.issue(any(UUID.class)))
                .willReturn("INV-2025-0001");

        mvc.perform(post("/api/v1/invoices/{id}/issue", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("INV-2025-0001"));
    }

    @Test
    void status_change_paid_returns_204() throws Exception {
        UUID id = UUID.randomUUID();
        var body = new InvoiceStatusChangeRequest(InvoiceStatus.PAID);

        // pokud je ve službě void metoda, tak ji umlčíme
        BDDMockito.willDoNothing().given(invoiceService)
                .changeStatus(any(UUID.class), any(InvoiceStatus.class));

        mvc.perform(post("/api/v1/invoices/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isNoContent());
    }

    @Test
    void upsert_lines_returns_204() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new InvoiceLinesUpsertRequest(List.of(
                new InvoiceLineDto("Práce", new BigDecimal("1"), "hod",
                        new BigDecimal("1000"), new BigDecimal("21"))
        ));

        // opět umlčíme případnou void metodu
        BDDMockito.willDoNothing().given(invoiceService)
                .addOrReplaceLines(any(UUID.class), any());

        mvc.perform(put("/api/v1/invoices/{id}/lines", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }
}

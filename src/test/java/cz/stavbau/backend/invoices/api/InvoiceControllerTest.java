package cz.stavbau.backend.invoices.api;

import cz.stavbau.backend.invoices.api.dto.InvoiceCreateRequest;
import cz.stavbau.backend.invoices.api.dto.InvoiceLinesUpsertRequest;
import cz.stavbau.backend.invoices.api.dto.InvoiceLineDto;
import cz.stavbau.backend.invoices.api.dto.InvoiceStatusChangeRequest;
import cz.stavbau.backend.invoices.model.InvoiceStatus;
import cz.stavbau.backend.invoices.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InvoiceController.class)
class InvoiceControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean InvoiceService invoiceService;

    @Test
    void create_draft_returns_id() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(invoiceService.createDraft(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString()))
               .thenReturn(id);

        var req = new InvoiceCreateRequest(
                UUID.randomUUID(), null,
                LocalDate.of(2025,9,12), LocalDate.of(2025,9,30), null,
                "CZK", "{}", "{}"
        );

        mvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id", is(id.toString())));
    }

    @Test
    void issue_returns_number() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(invoiceService.issue(id)).thenReturn("INV-2025-0001");

        mvc.perform(post("/api/v1/invoices/" + id + "/issue"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.number", is("INV-2025-0001")));
    }

    @Test
    void status_change_paid_returns_204() throws Exception {
        UUID id = UUID.randomUUID();
        var body = new InvoiceStatusChangeRequest(InvoiceStatus.PAID);

        mvc.perform(post("/api/v1/invoices/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
           .andExpect(status().isNoContent());
    }

    @Test
    void upsert_lines_returns_204() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new InvoiceLinesUpsertRequest(List.of(
                new InvoiceLineDto("Práce", new BigDecimal("1"), "hod", new BigDecimal("1000"), new BigDecimal("21"))
        ));
        mvc.perform(put("/api/v1/invoices/" + id + "/lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isNoContent());
    }
}

package cz.stavbau.backend.invoices.api;

import cz.stavbau.backend.features.invoices.api.InvoiceController;
import cz.stavbau.backend.features.invoices.model.Invoice;
import cz.stavbau.backend.features.invoices.model.InvoiceStatus;
import cz.stavbau.backend.features.invoices.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InvoiceController.class)
class InvoiceControllerRbacListTest {

    @Autowired MockMvc mvc;
    @MockBean InvoiceService invoiceService;

    private static RequestPostProcessor withAuth() {
        return request -> { request.addHeader("Authorization", "Bearer dummy"); return request; };
    }

    @Test
    void list_returns_paged_response() throws Exception {
        UUID company = UUID.randomUUID();
        Invoice inv = new Invoice();
        inv.setId(UUID.randomUUID());
        inv.setCompanyId(company);
        inv.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(invoiceService.search(Mockito.eq(company), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new PageImpl<>(List.of(inv), PageRequest.of(0,20), 1));

        mvc.perform(get("/api/v1/invoices").with(withAuth())
                .param("companyId", company.toString())
                .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.items", hasSize(1)))
           .andExpect(jsonPath("$.total", is(1)));
    }
}

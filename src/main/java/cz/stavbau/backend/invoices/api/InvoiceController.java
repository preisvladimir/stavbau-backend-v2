package cz.stavbau.backend.invoices.api;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {
    // TODO: endpoints per Step Plan + @PreAuthorize scopes
    @GetMapping public String list() { return "TODO"; }
}

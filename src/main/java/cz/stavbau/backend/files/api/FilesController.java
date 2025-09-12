package cz.stavbau.backend.files.api;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/files")
public class FilesController {
    // TODO: upload/list/download/delete/tags/link
    @GetMapping public String list() { return "TODO"; }
}

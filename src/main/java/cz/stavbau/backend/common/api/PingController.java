package cz.stavbau.backend.common.api;
import org.springframework.web.bind.annotation.*; import org.springframework.http.ResponseEntity;

@RestController @RequestMapping("/api/v1")
public class PingController {
    @GetMapping("/ping") public ResponseEntity<?> ping(){ return ResponseEntity.ok().body(java.util.Map.of("status","ok")); }
}

package cz.stavbau.backend.projects.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
public class UpsertProjectTranslationRequest {
    @Size(max = 255) private String name;
    private String description;
}

package cz.stavbau.backend.integrations.ares;

import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.integrations.ares.exceptions.AresNotFoundException;
import cz.stavbau.backend.integrations.ares.exceptions.AresUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AresClient {

    private final WebClient aresWebClient;

    public AresClient(WebClient.Builder builder) {
        this.aresWebClient = builder.baseUrl("hhttps://ares.gov.cz/ekonomicke-subjekty-v-be/rest").build();
    }

    public AresSubjectDto fetchByIco(String ico) {
        try {
            return aresWebClient.get()
                    .uri("/ekonomicke-subjekty/{ico}", ico)
                    .retrieve()
                    .onStatus(status -> status.value() == 404,
                            resp -> Mono.error(new AresNotFoundException("Firma s IČO " + ico + " nenalezena v ARES")))
                    .onStatus(status -> status.is5xxServerError(),
                            resp -> Mono.error(new AresUnavailableException("ARES nedostupný, zkuste později")))
                    .bodyToMono(AresSubjectDto.class)
                    .block();
        } catch (Exception e) {
            throw new AresUnavailableException("Chyba při komunikaci s ARES", e);
        }
    }
}

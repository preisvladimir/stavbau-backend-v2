package cz.stavbau.backend.integrations.ares;

import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDtoOld;
import cz.stavbau.backend.integrations.ares.exceptions.AresNotFoundException;
import cz.stavbau.backend.integrations.ares.exceptions.AresUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AresClient {

    private final WebClient aresWebClient;

    public AresSubjectDto fetchByIco(String ico) {
        try {
            return aresWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/ekonomicke-subjekty/{ico}").build(ico))
                    .retrieve()
                    // 404 -> AresNotFoundException
                    .onStatus(
                            status -> status.value() == HttpStatus.NOT_FOUND.value(),
                            resp -> Mono.error(new AresNotFoundException("Subjekt s IČO " + ico + " nebyl nalezen v ARES."))
                    )
                    // 5xx -> AresUnavailableException
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            resp -> Mono.error(new AresUnavailableException("ARES dočasně nedostupný (5xx)."))
                    )
                    .bodyToMono(AresSubjectDto.class)
                    .block();
        } catch (AresNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new AresUnavailableException("Chyba při volání ARES: " + e.getMessage(), e);
        }
    }

    public AresSubjectDtoOld fetchByIcoOld(String ico) {
        try {
            return aresWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/ekonomicke-subjekty/{ico}").build(ico))
                    .retrieve()
                    // 404 -> AresNotFoundException
                    .onStatus(
                            status -> status.value() == HttpStatus.NOT_FOUND.value(),
                            resp -> Mono.error(new AresNotFoundException("Subjekt s IČO " + ico + " nebyl nalezen v ARES."))
                    )
                    // 5xx -> AresUnavailableException
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            resp -> Mono.error(new AresUnavailableException("ARES dočasně nedostupný (5xx)."))
                    )
                    .bodyToMono(AresSubjectDtoOld.class)
                    .block();
        } catch (AresNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new AresUnavailableException("Chyba při volání ARES: " + e.getMessage(), e);
        }
    }

}

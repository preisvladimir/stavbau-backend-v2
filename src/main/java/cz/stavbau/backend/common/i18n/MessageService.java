package cz.stavbau.backend.common.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageService {
    private final MessageSource ms;
    private final I18nLocaleService i18nLocale;

    public String get(String key, Object... args) {
        Locale loc = i18nLocale.resolve();
        return ms.getMessage(key, args, key, loc);
    }
}

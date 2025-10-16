package cz.stavbau.backend.features.registrations.mailer;

import cz.stavbau.backend.features.registrations.service.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

public class MockMailer implements Mailer {
    private static final Logger log = LoggerFactory.getLogger(MockMailer.class);
    @Override
    public void sendVerificationEmail(String to, Locale locale, Map<String, Object> model) {
        log.info("[MOCK MAIL] to={} locale={} model={}", to, locale, model);
    }
}

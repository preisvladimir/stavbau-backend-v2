package cz.stavbau.backend.features.registrations.service;

import java.util.Locale;
import java.util.Map;

/**
 * Mailer rozhraní pro odesílání transakčních e-mailů v registracích.
 * V PR 4/7 je implementováno přes SMTP (SmtpMailer).
 */
public interface Mailer {

    /**
     * Odešle verifikační e-mail s odkazem na potvrzení.
     *
     * @param to    cílová e-mailová adresa
     * @param locale jazyk/locale pro subject/šablonu
     * @param model  data pro šablonu (např. "verificationLink", "expiresAt", "appName")
     */
    void sendVerificationEmail(String to, Locale locale, Map<String, Object> model);
}

package cz.stavbau.backend.features.registrations.mailer;

import cz.stavbau.backend.features.registrations.config.RegistrationsProperties;
import cz.stavbau.backend.features.registrations.service.Mailer;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SmtpMailer implements Mailer {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messages;
    private final RegistrationsProperties props;

    public SmtpMailer(JavaMailSender mailSender,
                      TemplateEngine templateEngine,
                      MessageSource messages,
                      RegistrationsProperties props) {
        this.mailSender = Objects.requireNonNull(mailSender);
        this.templateEngine = Objects.requireNonNull(templateEngine);
        this.messages = Objects.requireNonNull(messages);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public void sendVerificationEmail(String to, Locale locale, Map<String, Object> model) {
        try {
            // Model → Thymeleaf Context (doplníme lidsky čitelné expiresAtLocal)
            Context ctx = new Context(locale);
            if (model != null) {
                model.forEach(ctx::setVariable);
            }
            Instant expiresAt = model != null && model.get("expiresAt") instanceof Instant
                    ? (Instant) model.get("expiresAt") : null;
            if (expiresAt != null) {
                String expiresAtLocal = DateTimeFormatter
                        .ofPattern("d. M. yyyy H:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(expiresAt);
                ctx.setVariable("expiresAtLocal", expiresAtLocal);
            }
            ctx.setVariable("appName", "STAVBAU");

            String templatePrefix = props.getMail().getTemplatePrefix(); // např. "mail/registration/"
            String htmlBody = templateEngine.process(templatePrefix + "verify.html", ctx);
            String txtBody  = templateEngine.process(templatePrefix + "verify.txt",  ctx);

            String subjectKey = props.getMail().getVerifySubjectKey(); // "mail.registration.verify.subject"
            String subject = messages.getMessage(subjectKey, null, "Verify your email", locale);

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom(props.getMail().getFrom());
            helper.setSubject(subject);
            helper.setText(txtBody, htmlBody); // (text, html)

            mailSender.send(mime);
        } catch (Exception e) {
            // Tady neeskalujeme checked výjimky – necháme spadnout runtime, ať to servis vrstva zaloguje/řeší.
            throw new IllegalStateException("mail.send.failed", e);
        }
    }
}

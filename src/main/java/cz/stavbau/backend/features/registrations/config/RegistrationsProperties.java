package cz.stavbau.backend.features.registrations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "registrations")
public class RegistrationsProperties {

    private boolean enabled = true;
    private String publicBaseUrl;
    private boolean createCompanyUpfront = false;

    private Token token = new Token();
    private Case aCase = new Case();
    private Resend resend = new Resend();
    private RateLimit ratelimit = new RateLimit();
    private Captcha captcha = new Captcha();
    private Locales locales = new Locales();
    private Mail mail = new Mail();
    private Ares ares = new Ares();
    private Jobs jobs = new Jobs();

    @Setter
    @Getter
    public static class Token {
        private String ttl = "PT24H";
        private String secretRef = "REG_TOKEN_SECRET";

    }

    @Setter
    @Getter
    public static class Case {
        private String ttl = "P7D";

    }

    @Setter
    @Getter
    public static class Resend {
        private String cooldown = "PT5M";

    }

    @Getter
    public static class RateLimit {
        private String backend = "redis"; // redis|postgres
        private Start start = new Start();
        private ResendRate resend = new ResendRate();

        public void setBackend(String backend) { this.backend = backend; }

        @Setter
        @Getter
        public static class Start {
            private int perIpPerHour = 5;
            private int perEmailPerDay = 3;

        }
        @Setter
        @Getter
        public static class ResendRate {
            private int perEmailPerHour = 3;

        }
    }

    @Setter
    @Getter
    public static class Captcha {
        private String provider = "recaptcha"; // recaptcha|mock
        private double minScore = 0.5;
        private String siteKeyRef = "REG_RECAPTCHA_SITE_KEY";
        private String secretKeyRef = "REG_RECAPTCHA_SECRET_KEY";
        private boolean enforceOnResend = false;

    }

    @Setter
    @Getter
    public static class Locales {
        private List<String> allowed = List.of("cs-CZ", "en-US");

    }

    @Setter
    @Getter
    public static class Mail {
        private String provider = "smtp"; // smtp|queue|mock
        private String from = "STAVBAU <noreply@stavbau.cz>";
        private String templatePrefix = "mail/registration/";
        private String verifySubjectKey = "mail.registration.verify.subject";
        private String welcomeSubjectKey = "mail.registration.welcome.subject";

    }

    @Setter
    @Getter
    public static class Ares {
        private boolean enabled = true;
        private int timeoutMs = 3000;

    }

    @Setter
    @Getter
    public static class Jobs {
        private Expire expire = new Expire();
        public static class Expire {
        }
    }

    // getters/setters top-level
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    public boolean isCreateCompanyUpfront() { return createCompanyUpfront; }
    public void setCreateCompanyUpfront(boolean createCompanyUpfront) { this.createCompanyUpfront = createCompanyUpfront; }
    public Token getToken() { return token; }
    public Case getACase() { return aCase; }
    public Resend getResend() { return resend; }
    public RateLimit getRatelimit() { return ratelimit; }
    public Captcha getCaptcha() { return captcha; }
    public Locales getLocales() { return locales; }
    public Mail getMail() { return mail; }
    public Ares getAres() { return ares; }
    public Jobs getJobs() { return jobs; }
}

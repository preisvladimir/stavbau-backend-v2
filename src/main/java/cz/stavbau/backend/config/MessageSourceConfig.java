package cz.stavbau.backend.config;

import org.springframework.context.annotation.*; import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver; import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import java.util.*;

@Configuration
public class MessageSourceConfig {
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        var ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        return ms;
    }
    @Bean
    public LocaleResolver localeResolver() {
        var lr = new AcceptHeaderLocaleResolver();
        lr.setDefaultLocale(Locale.forLanguageTag("cs"));
        return lr;
    }
}

package cz.stavbau.backend.config;

import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.common.i18n.LocaleContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final I18nLocaleService i18nLocale;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                var loc = i18nLocale.resolve(request);
                LocaleContext.set(loc); // zpřístupníme pro service vrstvu
                return true;
            }
            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                LocaleContext.clear();
            }
        });
    }

    @Bean(name = "localeResolver")
    public org.springframework.web.servlet.LocaleResolver mvcLocaleResolver() {
        var r = new org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver();
        r.setDefaultLocale(java.util.Locale.forLanguageTag("cs-CZ"));
        return r;
    }

}

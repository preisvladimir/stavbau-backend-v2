package cz.stavbau.backend.config;

import cz.stavbau.backend.common.i18n.LocaleContext;
import cz.stavbau.backend.common.i18n.LocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LocaleResolver localeResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                var loc = localeResolver.resolve(request);
                LocaleContext.set(loc); // zpřístupníme pro service vrstvu
                return true;
            }
            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                LocaleContext.clear();
            }
        });
    }
}

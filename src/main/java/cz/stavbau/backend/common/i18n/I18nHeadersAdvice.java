package cz.stavbau.backend.common.i18n;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

@ControllerAdvice
public class I18nHeadersAdvice implements ResponseBodyAdvice<Object> {

    @Override public boolean supports(MethodParameter rt, Class<? extends HttpMessageConverter<?>> c) {
        return true; // pro všechny body
    }

    @Override
    public Object beforeBodyWrite(
            Object body, MethodParameter rt, MediaType sel, Class<? extends HttpMessageConverter<?>> c,
            ServerHttpRequest req, ServerHttpResponse res) {

        if (req instanceof org.springframework.http.server.ServletServerHttpRequest servletReq &&
                res instanceof ServletServerHttpResponse servletRes) {

            HttpServletRequest httpReq = servletReq.getServletRequest();
            HttpServletResponse httpRes = servletRes.getServletResponse();

            LocaleResolver lr = RequestContextUtils.getLocaleResolver(httpReq);
            Locale locale = (lr != null ? lr.resolveLocale(httpReq) : httpReq.getLocale());

            httpRes.setHeader("Content-Language", locale != null ? locale.toLanguageTag() : "cs");
            httpRes.addHeader("Vary", "Accept-Language");
        }
        return body;
    }
}
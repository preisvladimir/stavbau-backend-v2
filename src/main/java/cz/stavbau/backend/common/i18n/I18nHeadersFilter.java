package cz.stavbau.backend.common.i18n;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
public class I18nHeadersFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Locale locale = req.getLocale();
        res.setHeader("Content-Language", (locale != null ? locale.toLanguageTag() : "cs"));
        res.addHeader("Vary", "Accept-Language");
        chain.doFilter(req, res);
    }
}
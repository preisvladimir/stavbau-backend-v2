package cz.stavbau.backend.common.i18n;

import java.util.Locale;

/** Request-scoped držák rozřešené Locale. */
public class LocaleContext {
    private static final ThreadLocal<Locale> TL = new ThreadLocal<>();
    public static void set(Locale locale) { TL.set(locale); }
    public static Locale get() { return TL.get(); }
    public static void clear() { TL.remove(); }
}

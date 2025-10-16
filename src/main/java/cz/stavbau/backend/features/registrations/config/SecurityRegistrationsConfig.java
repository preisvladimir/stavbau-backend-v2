package cz.stavbau.backend.features.registrations.config;

// PR1: placeholder pro security nastavení.
// V PR5 doplníme SecurityFilterChain/Customizer, který povolí
// GET/POST /api/v1/public/registrations/**, vypne CSRF na této trase a nastaví CORS pro FE domény.
public class SecurityRegistrationsConfig {
    public static final String PUBLIC_BASE = "/api/v1/public/registrations/**";
    private SecurityRegistrationsConfig() {}
}

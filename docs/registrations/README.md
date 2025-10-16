# Registrations – modul

Cíl: profesionální, bezpečný a škálovatelný registrační systém pro vytvoření Company + OWNER.
- Source of truth: `registration_cases` (pending registrace).
- Tokeny: jednorázové, hashované, TTL.
- Stavový automat: EMAIL_SENT → EMAIL_VERIFIED → APPROVED → COMPANY_CREATED (+ EXPIRED/CANCELLED/FAILED).

## Flow (high-level)
1) POST /start → EMAIL_SENT + verifikační e-mail
2) Uživatel klikne na link → GET /confirm → vytvoření Company + OWNER
3) FE polluje GET /status/{id}

## Endpoints (public)
- `POST /api/v1/public/registrations/start`
- `POST /api/v1/public/registrations/send-confirm`
- `POST /api/v1/public/registrations/resend`
- `GET  /api/v1/public/registrations/confirm?token=...`
- `GET  /api/v1/public/registrations/status/{registrationId}`

# API Contract – Registrations

## RegistrationStartRequest (POST /start)
- ico?: string
- companyDraft: object { country, locale, name?, address?, settings?, preferences? }
- email: string
- password?: string
- consents: { terms: true, privacy: true, marketingOptIn?: boolean }
- captchaToken: string
- idempotencyKey?: string

## RegistrationResponse
- registrationId: UUID
- status: "EMAIL_SENT" | "EMAIL_VERIFIED" | "APPROVED" | "COMPANY_CREATED" | "EXPIRED" | "CANCELLED" | "FAILED"
- nextAction: "VERIFY_EMAIL" | "NONE"
- expiresAt: ISO-8601
- cooldownUntil?: ISO-8601
- messages?: [{ code, level, params? }]

## RegistrationStatusResponse
- registrationId: UUID
- status, nextAction, expiresAt, cooldownUntil?

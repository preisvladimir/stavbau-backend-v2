package cz.stavbau.backend.common.error;

public enum ErrorCode {
    COMPANY_EXISTS("company.exists"),
    USER_EMAIL_EXISTS("user.email.exists");
    private final String code;
    ErrorCode(String code) { this.code = code; }
    public String code() { return code; }
}

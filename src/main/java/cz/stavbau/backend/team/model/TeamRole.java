package cz.stavbau.backend.team.model;

public enum TeamRole {
    ADMIN, MEMBER;

    public static TeamRole fromString(String raw) {
        if (raw == null) return null;
        return TeamRole.valueOf(raw.trim().toUpperCase());
    }
}

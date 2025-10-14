package cz.stavbau.backend.features.members.model;

public enum MemberRole {
    ADMIN, MEMBER;

    public static MemberRole fromString(String raw) {
        if (raw == null) throw new IllegalArgumentException("role null");
        return MemberRole.valueOf(raw.trim().toUpperCase());
    }
}

package cz.stavbau.backend.identity.users.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // !!! companyId ODTEĎ NEPOUŽÍVEJ v kódu (ponecháno jen do kroku 2 migrace)
    // @Column(name = "company_id") private UUID companyId;

    @Column(name = "locale", length = 10)
    private String locale;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @Column(name = "refresh_token_id")
    private UUID refreshTokenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 16)
    private UserState state = UserState.ACTIVE;

    @Column(name = "password_needs_reset", nullable = false)
    private boolean passwordNeedsReset = false;

    @Column(name = "invited_at")
    private OffsetDateTime invitedAt;

    // nové – profil & preferences
    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "preferences", columnDefinition = "jsonb")
    private String preferences; // nebo @Type(JsonType) pokud používáš hibernate-types
}
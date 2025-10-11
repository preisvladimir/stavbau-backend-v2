// src/main/java/cz/stavbau/backend/common/util/CryptoUtils.java
package cz.stavbau.backend.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoUtils {
    private CryptoUtils() {}
    private static final SecureRandom RNG = new SecureRandom();

    /** URL-safe base64 bez paddingu; default 24 B ~ 32 znaků */
    public static String randomUrlSafeSecret(int bytes) {
        byte[] buf = new byte[Math.max(1, bytes)];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    public static String randomUrlSafeSecret() {
        return randomUrlSafeSecret(24);
    }
}

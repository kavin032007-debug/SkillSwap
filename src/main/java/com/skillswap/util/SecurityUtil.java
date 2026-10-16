package com.skillswap.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security and sanitization utility for PIN hashing, token generation, and HTML escaping.
 */
public final class SecurityUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurityUtil() {}

    /**
     * Generates a random cryptographic salt.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(salt);
    }

    /**
     * Generates a secure random session token.
     */
    public static String generateToken() {
        byte[] token = new byte[24];
        RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    /**
     * Computes SHA-256 hash of PIN combined with salt.
     */
    public static String hashPin(String pin, String salt) {
        if (pin == null) pin = "";
        if (salt == null) salt = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    /**
     * Verifies if raw PIN matches hashed PIN with given salt.
     */
    public static boolean verifyPin(String rawPin, String salt, String expectedHash) {
        if (rawPin == null || expectedHash == null) {
            return false;
        }
        String calculated = hashPin(rawPin, salt);
        return MessageDigest.isEqual(
            calculated.getBytes(StandardCharsets.UTF_8),
            expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Escapes untrusted text to safely render inside HTML content or attributes,
     * preventing Cross-Site Scripting (XSS).
     */
    public static String escapeHtml(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                case '/' -> sb.append("&#x2F;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}

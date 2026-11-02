package com.example.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
    private Passwords() {}
    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return "pbkdf2$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derive(password, salt));
    }
    public static boolean matches(String password, String stored) {
        if (password == null || stored == null) return false;
        if (!stored.startsWith("pbkdf2$")) return MessageDigest.isEqual(password.getBytes(java.nio.charset.StandardCharsets.UTF_8), stored.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        try {
            String[] parts = stored.split("\\$");
            return MessageDigest.isEqual(derive(password, Base64.getDecoder().decode(parts[1])), Base64.getDecoder().decode(parts[2]));
        } catch (RuntimeException ex) { return false; }
    }
    private static byte[] derive(String password, byte[] salt) {
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(password.toCharArray(), salt, 210000, 256)).getEncoded(); }
        catch (Exception ex) { throw new IllegalStateException("Password hashing failed", ex); }
    }
}

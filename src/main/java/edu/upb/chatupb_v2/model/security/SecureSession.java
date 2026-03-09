package edu.upb.chatupb_v2.model.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class SecureSession {
    private static final SecureRandom RNG = new SecureRandom();
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecurityProtocol securityProtocol;
    private final SecretKeySpec key;

    public SecureSession(SecurityProtocol suite, byte[] keyBytes) {
        this.securityProtocol = suite;
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public SecurityProtocol getSecurityProtocol() { return securityProtocol; }


    public String encryptToWire(String plain) throws Exception {
        byte[] nonce = new byte[NONCE_BYTES];
        RNG.nextBytes(nonce);

        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
        byte[] cipher = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(nonce) + ":" +
                Base64.getEncoder().encodeToString(cipher);
    }

    public String decryptFromWire(String wire) throws Exception {
        String[] parts = wire.split(":", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Payload cifrado inválido");

        byte[] nonce = Base64.getDecoder().decode(parts[0]);
        byte[] cipher = Base64.getDecoder().decode(parts[1]);

        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
        byte[] plain = c.doFinal(cipher);

        return new String(plain, StandardCharsets.UTF_8);
    }
}
package edu.upb.chatupb_v2.model.security;

public enum SecurityProtocol {
    AES_128(16), AES_256(32);

    private final int keyBytes;

    SecurityProtocol(int keyBytes) { this.keyBytes = keyBytes; }

    public int keyBytes() { return keyBytes; }

    public static SecurityProtocol fromWire(String s) {
        return SecurityProtocol.valueOf(s.trim());
    }
}
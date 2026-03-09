package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter @Setter
public class SecureKey extends Message {

    private String suite;
    private String keyBase64;

    public SecureKey() { super("015"); }

    public SecureKey(String suite, String keyBase64) {
        super("015");
        this.suite = suite;
        this.keyBase64 = keyBase64;
    }

    public static SecureKey parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 3) throw new IllegalArgumentException("Formato 015 inválido");
        return new SecureKey(split[1], split[2]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + suite + "|" + keyBase64 + System.lineSeparator();
    }
}
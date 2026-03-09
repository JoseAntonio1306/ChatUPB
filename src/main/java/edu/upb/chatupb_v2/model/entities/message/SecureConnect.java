package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter @Setter
public class SecureConnect extends Message {

    private String suite1;
    private String suite2;

    public SecureConnect() { super("014"); }

    public SecureConnect(String suite1, String suite2) {
        super("014");
        this.suite1 = suite1;
        this.suite2 = suite2;
    }

    public static SecureConnect parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 3) throw new IllegalArgumentException("Formato 014 inválido");
        return new SecureConnect(split[1], split[2]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + suite1 + "|" + suite2 + System.lineSeparator();
    }
}
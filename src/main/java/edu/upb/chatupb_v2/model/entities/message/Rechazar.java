package edu.upb.chatupb_v2.model.entities.message;

import java.util.regex.Pattern;

public class Rechazar extends Message{

    public Rechazar() {
        super("003");
    }

    public static Rechazar parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 1) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Rechazar();
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + System.lineSeparator();
    }


}

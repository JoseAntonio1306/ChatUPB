package edu.upb.chatupb_v2.bl.message;

import java.util.regex.Pattern;

public class Offline extends Message{
    private String idUsuario;

    public Offline() {
        super("0018");
    }
    public Offline(String idUsuario) {
        super("0018");
        this.idUsuario = idUsuario;
    }

    public static Offline parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Offline(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario + System.lineSeparator();
    }

    public String getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

}

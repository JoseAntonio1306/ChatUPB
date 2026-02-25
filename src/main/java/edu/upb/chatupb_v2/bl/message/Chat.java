package edu.upb.chatupb_v2.bl.message;

import java.util.regex.Pattern;

public class Chat extends Message{
    private String idUsuario;
    private String idMensaje;
    private String mensaje;

    public Chat() {
        super("007");
    }
    public Chat(String idUsuario, String idMensaje, String mensaje) {
        super("007");
        this.idUsuario = idUsuario;
        this.idMensaje = idMensaje;
        this.mensaje = mensaje;
    }

    public static Chat parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 4) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Chat(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" + idMensaje + "|" + mensaje + System.lineSeparator();
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

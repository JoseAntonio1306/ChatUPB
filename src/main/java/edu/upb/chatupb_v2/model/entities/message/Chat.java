package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
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

}

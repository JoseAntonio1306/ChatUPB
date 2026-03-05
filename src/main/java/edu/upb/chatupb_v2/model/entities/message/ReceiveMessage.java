package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class ReceiveMessage extends Message{
    private String idMensaje;

    public ReceiveMessage() {
        super("008");
    }
    public ReceiveMessage(String idMensaje) {
        super("008");
        this.idMensaje = idMensaje;
    }

    public static ReceiveMessage parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new ReceiveMessage(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + idMensaje + System.lineSeparator();
    }
}

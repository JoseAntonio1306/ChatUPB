package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;
@Getter
@Setter
public class AcceptHello extends Message{
    private String idUsuario;

    public AcceptHello() {
        super("005");
    }
    public AcceptHello(String idUsuario) {
        super("005");
        this.idUsuario = idUsuario;
    }

    public static AcceptHello parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new AcceptHello(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario + System.lineSeparator();
    }

}

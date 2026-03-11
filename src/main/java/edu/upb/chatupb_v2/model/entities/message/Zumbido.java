package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Zumbido extends Message{
    private String idUser;

    public Zumbido() {
        super("010");
    }
    public Zumbido(String idUser) {
        super("010");
        this.idUser = idUser;
    }

    public static Zumbido parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Zumbido(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUser + System.lineSeparator();
    }

}

package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Invitacion extends AbstractMessage {

    private String idUsuario;
    private String nombre;

    public Invitacion() {
        super("001");
    }
    public Invitacion(String idUsuario, String nombre) {
        super("001");
        this.idUsuario = idUsuario;
        this.nombre = nombre;
    }

    public static Invitacion parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 3) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Invitacion(split[1], split[2]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" +nombre + System.lineSeparator();
    }

}

package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Aceptar extends AbstractMessage {
    private String idUsuario;
    private String nombre;

    public Aceptar() {
        super("002");
    }
    public Aceptar(String idUsuario, String nombre) {
        super("002");
        this.idUsuario = idUsuario;
        this.nombre = nombre;
    }

    public static Aceptar parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 3) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Aceptar(split[1], split[2]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" +nombre + System.lineSeparator();
    }

}

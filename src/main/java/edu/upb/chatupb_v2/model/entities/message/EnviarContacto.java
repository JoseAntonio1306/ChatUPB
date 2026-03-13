package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class EnviarContacto extends AbstractMessage {
    private String idUsuario;
    private String nombre;
    private String ip;

    public EnviarContacto() {
        super("020");
    }
    public EnviarContacto(String idUsuario, String nombre, String ip) {
        super("020");
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.ip = ip;
    }

    public static EnviarContacto parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 4) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new EnviarContacto(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" + nombre + "|" + ip + System.lineSeparator();
    }
}

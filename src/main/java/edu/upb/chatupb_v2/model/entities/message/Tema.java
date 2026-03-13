package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Pattern;

@Getter
@Setter
public class Tema extends AbstractMessage {
    private String idUsuario;
    private String idTema;

    public Tema() {
        super("013");
    }
    public Tema(String idUsuario, String idTema) {
        super("013");
        this.idUsuario = idUsuario;
        this.idTema = idTema;
    }

    public static Tema parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 4) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Tema(split[1], split[2]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" + idTema + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) throws IOException {
        client.send(this);
    }
}

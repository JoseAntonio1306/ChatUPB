package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Pattern;

@Getter
@Setter
public class Offline extends AbstractMessage {
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

    @Override
    public void execute(SocketClient client) throws IOException {
        client.send(this);
    }
}

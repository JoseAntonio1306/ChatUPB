package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Pattern;

@Getter
@Setter
public class FijarMensaje extends AbstractMessage {
    private String idMensaje;

    public FijarMensaje() {
        super("011");
    }
    public FijarMensaje(String idMensaje) {
        super("011");
        this.idMensaje = idMensaje;
    }

    public static FijarMensaje parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new FijarMensaje(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idMensaje + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) throws IOException {
        client.send(this);
    }
}

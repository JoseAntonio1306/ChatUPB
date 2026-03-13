package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;

import java.io.IOException;
import java.util.regex.Pattern;

public class RejectHello extends AbstractMessage {
    public RejectHello() {
        super("006");
    }

    public static RejectHello parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 1) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new RejectHello();
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) throws IOException {
        client.send(this);
    }
}

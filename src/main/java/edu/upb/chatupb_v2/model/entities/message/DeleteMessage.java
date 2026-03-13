package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Pattern;

@Getter
@Setter
public class DeleteMessage extends AbstractMessage {
    private String idMensaje;

    public DeleteMessage() {
        super("009");
    }
    public DeleteMessage(String idMensaje) {
        super("009");
        this.idMensaje = idMensaje;
    }

    public static DeleteMessage parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new DeleteMessage(split[1]);
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

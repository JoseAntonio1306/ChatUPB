package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
public abstract class AbstractMessage {
    private String codigo;
    private String ip;

    public AbstractMessage(String codigo) {
        this.codigo = codigo;
    }
    public abstract String generarTrama();
    public abstract void execute(SocketClient client) throws IOException;
}

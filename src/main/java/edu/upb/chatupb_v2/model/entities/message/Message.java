package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Message {
    private String codigo;
    private String ip;

    public Message(String codigo) {
        this.codigo = codigo;
    }
    public abstract String generarTrama();
//    public abstract void execute(SocketClient client);
}

package edu.upb.chatupb_v2.model.entities.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractMessage {
    private String codigo;
    private String ip;

    public AbstractMessage(String codigo) {
        this.codigo = codigo;
    }
    public abstract String generarTrama();
//    public abstract void execute(SocketClient client);
}

package edu.upb.chatupb_v2.model.server;

import edu.upb.chatupb_v2.model.entities.message.AbstractMessage;

public interface SocketListener {
    void onMessage(SocketClient socketClient, AbstractMessage invitacion);
}

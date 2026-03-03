package edu.upb.chatupb_v2.model.server;

import edu.upb.chatupb_v2.model.entities.message.Message;

public interface SocketListener {
    void onMessage(SocketClient socketClient,Message invitacion);
}

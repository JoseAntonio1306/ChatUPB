package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.Invitacion;
import edu.upb.chatupb_v2.bl.message.Message;

public interface SocketListener {
    void onMessage(SocketClient socketClient,Message invitacion);
}

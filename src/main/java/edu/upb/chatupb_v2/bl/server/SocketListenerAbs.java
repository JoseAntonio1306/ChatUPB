package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.Message;

public abstract class SocketListenerAbs {
    protected final Mediador mediador;

    protected SocketListenerAbs() {
        this(Mediador.getInstance());
    }

    protected SocketListenerAbs(Mediador mediador) {
        this.mediador = mediador;
    }

    public abstract void onMessage(SocketClient socketClient, Message message);

    // Helpers mínimos para usar Mediador sin repetir getInstance()
    protected final void registerClient(String userId, SocketClient client) {
        mediador.addClient(userId, client);
    }

    protected final void sendTo(String userId, Message message) {
        mediador.sendMessage(userId, message);
    }

}

package edu.upb.chatupb_v2.model.server;

import edu.upb.chatupb_v2.model.entities.message.Invitacion;
import edu.upb.chatupb_v2.model.entities.message.Message;
import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.view.IChatView;

import java.io.IOException;
import java.util.HashMap;

public class Mediador implements SocketListener{
    private static final Mediador INSTANCE = new Mediador();
    private volatile IChatView view;
    private final HashMap<String, SocketClient> clients = new HashMap<>();

    private Mediador(){}

    public static Mediador getInstance(){
        return INSTANCE;
    }

    public void addClient(String userId, SocketClient client){
        this.clients.put(userId, client);
    }

    public void removeClient(String userId){
        this.clients.remove(userId);
    }

    public void sendMessage(String userId, Message message){
        SocketClient client = this.clients.get(userId);
        if (client == null) return;

        try{
            client.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Enviando mensaje: " + message.generarTrama());
    }

    public void invitacion(String ip){
        SocketClient client;
        try{
            client = new SocketClient(ip);
            client.addListener(this);
            client.start();

        } catch (Exception e) {
            throw new OperationException("No se logro establecer la conexion");
        }
        Invitacion invitacion = new Invitacion();
        invitacion.setIdUsuario("MI_ID");
        invitacion.setNombre("MI_NOMBRE");
        try {
            client.send(invitacion);
        } catch (IOException e) {
            throw new OperationException("No se logro enviar el mensaje ");
        }
    }

    @Override
    public void onMessage(SocketClient socketClient, Message message) {
        // El Mediador recibe TODOS los mensajes desde los SocketClient.
        // Reenvía a la vista principal para que actualice la UI.
        IChatView v = this.view;
        if (v != null) {
            v.onSocketMessage(socketClient, message);
        }
    }

    public void setView(IChatView view) {
        this.view = view;
    }

}

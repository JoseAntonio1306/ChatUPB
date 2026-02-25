package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.Message;

import java.util.HashMap;

public class Mediador{

    private static final Mediador INSTANCE = new Mediador();

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



}

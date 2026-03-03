/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.model.server.ChatServer;
import edu.upb.chatupb_v2.model.server.Mediador;
import edu.upb.chatupb_v2.view.MainChatUI;

/**
 * @author rlaredo
 */
public class ChatUPB_V2 {

    public static void main(String[] args) {

        final MainChatUI mainUI = new MainChatUI();
        java.awt.EventQueue.invokeLater(() -> mainUI.setVisible(true));

        try {
            ChatServer chatServer = new ChatServer();
            Mediador.getInstance().setView(mainUI);
            chatServer.addListener(Mediador.getInstance());
//            chatServer.addListener(mainUI); // primero listener
            chatServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* Create and display the form */
//        final ChatUI chatUI = new ChatUI();
//        java.awt.EventQueue.invokeLater(() -> chatUI.setVisible(true));
//
//        try {
//            ChatServer chatServer = new ChatServer();
//            chatServer.start();
//            chatServer.addListener(chatUI);
//        }catch (Exception e){
//            e.printStackTrace();
//        }



    }
}

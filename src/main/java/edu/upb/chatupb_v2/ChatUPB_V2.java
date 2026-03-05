package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.model.server.ChatServer;
import edu.upb.chatupb_v2.model.server.Mediador;
import edu.upb.chatupb_v2.model.settings.UserSettings;
import edu.upb.chatupb_v2.view.MainChatUI;

import javax.swing.JOptionPane;

public class ChatUPB_V2 {

    public static void main(String[] args) {

//        UserSettings.clearUsername();
        String userId = UserSettings.createUserId();
        String nombre = UserSettings.getUsername();

        if (nombre == null || nombre.isBlank()) {

            while (true) {
                String input = JOptionPane.showInputDialog(
                        null,
                        "Ingresa tu nombre de usuario:",
                        "Registro de Usuario",
                        JOptionPane.QUESTION_MESSAGE
                );

                if (input == null) { // canceló
                    return;
                }

                input = input.trim();

                if (input.isBlank()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "El nombre no puede estar vacío. Por favor, ingrésalo.",
                            "Campo vacío",
                            JOptionPane.WARNING_MESSAGE
                    );
                    continue;
                }

                if (input.length() > 60) {
                    JOptionPane.showMessageDialog(
                            null,
                            "El nombre no puede tener más de 60 caracteres. (Tienes " + input.length() + ")",
                            "Límite de caracteres",
                            JOptionPane.WARNING_MESSAGE
                    );
                    continue;
                }

                nombre = input;
                UserSettings.setUsername(nombre);
                break;
            }
        }
        //Crear UI con el nombre
        final MainChatUI mainUI = new MainChatUI(userId, nombre);
        java.awt.EventQueue.invokeLater(() -> mainUI.setVisible(true));

        //Iniciar servidor + mediador
        try {
            ChatServer chatServer = new ChatServer();
            Mediador.getInstance().setView(mainUI);
            chatServer.addListener(Mediador.getInstance());
            chatServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
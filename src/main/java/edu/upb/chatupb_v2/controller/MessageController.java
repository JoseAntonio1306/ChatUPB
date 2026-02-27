package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.bl.message.Chat;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.MessageDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageController {

    private final MessageDao messageDao;
    private final IChatView chatView;
    private final String myUserId;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MessageController(IChatView chatView, String myUserId) {
        this.messageDao = new MessageDao();
        this.chatView = chatView;
        this.myUserId = myUserId;
    }

    //cargar historial desde DB
    public void onOpenConversation(Contact contact) {
        try {
            List<Message> messages = messageDao.findConversation(myUserId, contact.getCode());
            String chat = buildChatHistory(contact.getCode(), messages);
            chatView.onChatHistoryLoaded(contact.getCode(), chat);
        } catch (Exception e) {
            System.out.println("Error cargando historial: " + e.getMessage());
            chatView.onChatHistoryLoaded(contact.getCode(), "");
        }
    }

    // cuando envías un mensaje se lo guarda en DB
    public void onOutgoingMessage(Contact contact, String idMensaje, String texto) {
        try {
            Message message = Message.builder()
                    .codMessage(idMensaje)
                    .senderCode(myUserId)
                    .recipientCode(contact.getCode())
                    .createdDate(LocalDateTime.now().format(fmt))
                    .message(texto)
                    .type("CHAT")
                    .roomCode(null)
                    .build();

            messageDao.save(message);
        } catch (Exception e) {
            System.out.println("Error guardando mensaje: " + e.getMessage());
        }
    }

    // cuando recibes un mensaje lo guarda en DB
    public void onIncomingMessage(Chat chat) {
        try {
            Message message = Message.builder()
                    .codMessage(chat.getIdMensaje())
                    .senderCode(chat.getIdUsuario())
                    .recipientCode(myUserId)
                    .createdDate(LocalDateTime.now().format(fmt))
                    .message(chat.getMensaje())
                    .type("CHAT")
                    .roomCode(null)
                    .build();

            messageDao.save(message);
        } catch (Exception e) {
            System.out.println("Error guardando el mensaje entrante: " + e.getMessage());
        }
    }

    private String buildChatHistory(String otherCode, List<Message> msgs) {
        StringBuilder sb = new StringBuilder();
        for (Message m : msgs) {
            boolean mine = myUserId.equals(m.getSenderCode());
            sb.append(mine ? "Yo: " : "Él: ")
                    .append(m.getMessage() == null ? "" : m.getMessage())
                    .append("\n");
        }
        return sb.toString();
    }
}
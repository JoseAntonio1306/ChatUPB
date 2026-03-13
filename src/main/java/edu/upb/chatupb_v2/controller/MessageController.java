package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.dao.MessageDao;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.message.Chat;
import edu.upb.chatupb_v2.view.IChatView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class MessageController {

    private final MessageDao messageDao;
    private final IChatView chatView;
    private final String myUserId;

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MessageController(IChatView chatView, String myUserId) {
        this.messageDao = new MessageDao();
        this.chatView = chatView;
        this.myUserId = myUserId;
    }

    public void onOpenConversation(Contact contact) {
        try {
            List<Message> messages = messageDao.findConversation(myUserId, contact.getCode());
            chatView.onChatHistoryLoaded(contact.getCode(), messages);
        } catch (Exception e) {
            System.out.println("Error cargando historial: " + e.getMessage());
            chatView.onChatHistoryLoaded(contact.getCode(), Collections.emptyList());
        }
    }

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
}
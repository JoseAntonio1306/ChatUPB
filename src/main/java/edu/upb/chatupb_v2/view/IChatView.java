package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.message.AbstractMessage;

import java.util.List;

public interface IChatView {
    void setContactController(ContactController contactController);

    void onLoad(List<Contact> contactsList);

    void setMessageController(MessageController messageController);

    void onChatHistoryLoaded(
            String contactCode,
            List<edu.upb.chatupb_v2.model.entities.Message> messages
    );

    void onSocketMessage(AbstractMessage message);

    void onContactStatusChanged(String contactCode, boolean online);
}
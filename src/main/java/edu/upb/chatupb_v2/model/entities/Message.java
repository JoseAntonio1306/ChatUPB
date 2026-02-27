package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.Model;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable, Model {

    public static final class Column {
        public static final String ID = "id";
        public static final String COD_MESSAGE = "cod_message";
        public static final String RECIPIENT_CODE = "recipient_code";
        public static final String CREATED_DATE = "created_date";
        public static final String SENDER_CODE = "sender_code";
        public static final String MESSAGE = "message";
        public static final String TYPE = "type";
        public static final String ROOM_CODE = "room_code";
    }

    private long id;
    private String codMessage;
    private String recipientCode;
    private String createdDate;
    private String senderCode;
    private String message;
    private String type;
    private String roomCode;

    @Override
    public void setId(long id) {
        this.id = id;
    }
}
package edu.upb.chatupb_v2.model.dao;

import edu.upb.chatupb_v2.model.db.DaoHelper;
import edu.upb.chatupb_v2.model.entities.Message;

import java.sql.PreparedStatement;
import java.util.List;

public class MessageDao {

    private final DaoHelper<Message> helper;

    public MessageDao() {
        this.helper = new DaoHelper<>();
    }

    private final DaoHelper.ResultReader<Message> reader = rs -> Message.builder()
            .id(rs.getLong(Message.Column.ID))
            .codMessage(rs.getString(Message.Column.COD_MESSAGE))
            .recipientCode(rs.getString(Message.Column.RECIPIENT_CODE))
            .createdDate(rs.getString(Message.Column.CREATED_DATE))
            .senderCode(rs.getString(Message.Column.SENDER_CODE))
            .message(rs.getString(Message.Column.MESSAGE))
            .type(rs.getString(Message.Column.TYPE))
            .roomCode(rs.getString(Message.Column.ROOM_CODE))
            .build();

    public void save(Message msg) throws Exception {
        String sql = """
                INSERT INTO message(cod_message, recipient_code, created_date, sender_code, message, type, room_code)
                VALUES(?,?,?,?,?,?,?)
                """;

        DaoHelper.QueryParameters params = (PreparedStatement pst) -> {
            pst.setString(1, msg.getCodMessage());
            pst.setString(2, msg.getRecipientCode());
            pst.setString(3, msg.getCreatedDate());
            pst.setString(4, msg.getSenderCode());
            pst.setString(5, msg.getMessage());
            pst.setString(6, msg.getType());
            pst.setString(7, msg.getRoomCode()); // puede ser null
        };

        helper.insert(sql, params, msg);
    }

    public List<Message> findConversation(String myCode, String otherCode) throws Exception {
        String sql = """
        SELECT * FROM message
        WHERE room_code IS NULL
          AND (
                (sender_code=? AND recipient_code=?)
             OR (sender_code=? AND recipient_code=?)
          )
        ORDER BY created_date ASC, id ASC
        """;

        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, myCode);
            pst.setString(2, otherCode);
            pst.setString(3, otherCode);
            pst.setString(4, myCode);
        };

        return helper.executeQuery(sql, params, reader);
    }
}
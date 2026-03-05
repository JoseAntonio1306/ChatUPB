package edu.upb.chatupb_v2.model.dao;

import edu.upb.chatupb_v2.model.db.DaoHelper;
import edu.upb.chatupb_v2.model.entities.Contact;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ContactDao {

    private final DaoHelper<Contact> helper;

    public ContactDao() {
        this.helper = new DaoHelper<>();
    }

    private final DaoHelper.ResultReader<Contact> resultReader = result -> {
        Contact contact = new Contact();
        if (existColumn(result, Contact.Column.ID)) {
            contact.setId(result.getLong(Contact.Column.ID));
        }
        if (existColumn(result, Contact.Column.CODE)) {
            contact.setCode(result.getString(Contact.Column.CODE));
        }
        if (existColumn(result, Contact.Column.NAME)) {
            contact.setName(result.getString(Contact.Column.NAME));
        }
        if (existColumn(result, Contact.Column.IP)) {
            contact.setIp(result.getString(Contact.Column.IP));
        }
        return contact;
    };

    public static boolean existColumn(ResultSet result, String columnName) {
        try {
            result.findColumn(columnName);
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public List<Contact> findAll() throws ConnectException, SQLException {
        return helper.executeQuery("SELECT * FROM contact", resultReader);
    }

    public boolean existByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM contact WHERE code=?";
        DaoHelper.QueryParameters params = pst -> pst.setString(1, code);
        return helper.executeQueryCount(query, params) == 1;
    }

    public Contact findByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT * FROM contact WHERE code=?";
        DaoHelper.QueryParameters params = pst -> pst.setString(1, code);
        List<Contact> list = helper.executeQuery(query, params, resultReader);
        return list.isEmpty() ? null : list.get(0);
    }

    public void save(Contact contact) throws Exception {
        String query = "INSERT INTO contact(code, name, ip) values (?,?,?)";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, contact.getCode());
                pst.setString(2, contact.getName());
                pst.setString(3, contact.getIp());
            }
        };
        helper.insert(query, params, contact);
    }

    public void update(Contact contact) throws Exception {
        // actualiza nombre e ip
        String query = "UPDATE contact SET name=?, ip=? WHERE code=?";
        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, contact.getName());
            pst.setString(2, contact.getIp());
            pst.setString(3, contact.getCode());
        };
        helper.update(query, params);
    }

    /**
     * Inserta si no existe, o actualiza si ya está.
     */
    public Contact upsert(String code, String name, String ip) throws Exception {
        Contact existing = findByCode(code);
        if (existing == null) {
            Contact created = Contact.builder()
                    .code(code)
                    .name(name)
                    .ip(ip)
                    .stateConnect(false)
                    .build();
            save(created);
            return created;
        }
        existing.setName(name);
        existing.setIp(ip);
        update(existing);
        return existing;
    }
    public void deleteByCode(String code) throws Exception {
        String query = "DELETE FROM contact WHERE code=?";
        DaoHelper.QueryParameters params = pst -> pst.setString(1, code);
        helper.update(query, params);
    }
}

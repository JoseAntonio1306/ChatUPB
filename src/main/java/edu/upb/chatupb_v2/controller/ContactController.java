package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.dao.ContactDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.List;

public class ContactController {
    private ContactDao contactDao;
    private IChatView chatView;

    public ContactController(IChatView chatView) {
        this.contactDao = new ContactDao();
        this.chatView = chatView;
    }

    public void onLoad(){
        try{
            List<Contact> contacts = contactDao.findAll();
            chatView.onLoad(contacts);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}

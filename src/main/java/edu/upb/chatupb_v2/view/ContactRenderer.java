package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Contact;

import javax.swing.*;
import java.awt.*;

public class ContactRenderer extends JLabel implements ListCellRenderer<Contact> {

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Contact> list,
            Contact contact,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(
                contact.isStateConnect() ? "/images/on-line.png" : "/images/off-line.png"
        ));

        Image imgScaled = imageIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(imgScaled));

        String name = (contact.getName() == null || contact.getName().isBlank())
                ? contact.getCode()
                : contact.getName();

        setText("<html><b>" + escape(name) + "</b></html>");

        if (isSelected) {
            setOpaque(true);
            setBackground(new Color(230, 230, 250));
        } else {
            setOpaque(true);
            setBackground(Color.WHITE);
        }

        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        return this;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

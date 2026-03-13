package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.view.components.StatusIcon;

import javax.swing.*;
import java.awt.*;

public class ContactRenderer extends DefaultListCellRenderer {

    private static final Color COLOR_ONLINE = new Color(34, 197, 94);
    private static final Color COLOR_OFFLINE = new Color(239, 68, 68);
    private static final Color COLOR_BORDER = new Color(148, 163, 184);

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus
        );

        if (value instanceof Contact contact) {
            label.setText(contact.getName());
            label.setIcon(new StatusIcon(
                    contact.isStateConnect() ? COLOR_ONLINE : COLOR_OFFLINE,
                    COLOR_BORDER,
                    12
            ));
        }

        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        label.setIconTextGap(10);

        if (isSelected) {
            label.setBackground(new Color(224, 231, 255));
            label.setForeground(new Color(30, 41, 59));
        } else {
            label.setBackground(Color.WHITE);
            label.setForeground(new Color(30, 41, 59));
        }

        return label;
    }
}
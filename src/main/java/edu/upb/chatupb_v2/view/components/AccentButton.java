package edu.upb.chatupb_v2.view.components;

import javax.swing.*;
import java.awt.*;

import static edu.upb.chatupb_v2.view.MainChatUI.*;

public class AccentButton extends JButton {
    public AccentButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        if (getModel().isPressed()) {
            g2.setColor(new Color(79, 70, 229));
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(55, 48, 163));
        } else {
            g2.setColor(new Color(67, 56, 202));
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();

        super.paintComponent(g);
    }
}

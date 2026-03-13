package edu.upb.chatupb_v2.view.components;

import javax.swing.*;
import java.awt.*;

public class RoundButton extends JButton {
    public RoundButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2.setColor(new Color(27, 163, 76));
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(43, 218, 109));
        } else {
            g2.setColor(new Color(37, 211, 102));
        }

        g2.fillOval(0, 0, getWidth(), getHeight());
        g2.dispose();

        super.paintComponent(g);
    }

    @Override
    public boolean contains(int x, int y) {
        int radius = getWidth() / 2;
        int centerX = radius;
        int centerY = radius;

        return Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= Math.pow(radius, 2);
    }
}
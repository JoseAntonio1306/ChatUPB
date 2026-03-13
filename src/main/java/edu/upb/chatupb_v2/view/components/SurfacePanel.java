package edu.upb.chatupb_v2.view.components;

import javax.swing.*;
import java.awt.*;

import static javax.swing.text.StyleConstants.setBackground;

public class SurfacePanel extends JPanel {
    private final int radius;
    private final Color borderColor;

    public SurfacePanel(int radius, Color backgroundColor, Color borderColor) {
        this.radius = radius;
        this.borderColor = borderColor;
        setBackground(backgroundColor);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
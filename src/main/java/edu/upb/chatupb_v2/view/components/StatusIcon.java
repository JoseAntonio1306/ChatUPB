package edu.upb.chatupb_v2.view.components;

import javax.swing.*;
import java.awt.*;

public class StatusIcon implements Icon {

    private final Color fillColor;
    private final Color borderColor;
    private final int size;

    public StatusIcon(Color fillColor, Color borderColor, int size) {
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(fillColor);
        g2.fillOval(x, y, size, size);

        g2.setColor(borderColor);
        g2.drawOval(x, y, size, size);

        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
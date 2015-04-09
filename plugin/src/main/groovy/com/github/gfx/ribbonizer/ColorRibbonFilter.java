package com.github.gfx.ribbonizer;

import com.android.build.gradle.api.ApplicationVariant;

import org.gradle.api.Action;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ColorRibbonFilter implements Action<BufferedImage> {
    static final boolean debug = Boolean.parseBoolean(System.getenv("RIBBONIZER_DEBUG"));

    final Color ribbonColor;
    final Color labelColor;

    String label;
    String fontName = "Default";
    int fontStyle = Font.PLAIN;

    public ColorRibbonFilter(String label, Color ribbonColor, Color labelColor) {
        this.label = label;
        this.ribbonColor = ribbonColor;
        this.labelColor = labelColor;
    }

    public ColorRibbonFilter(ApplicationVariant variant, Color ribbonColor, Color labelColor) {
        this(variant.getBuildType().getName(), ribbonColor, labelColor);
    }

    public ColorRibbonFilter(ApplicationVariant variant, Color ribbonColor) {
        this(variant, ribbonColor, Color.WHITE);
    }


    @Override
    public void execute(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Graphics2D g = (Graphics2D)image.getGraphics();

        g.setTransform(AffineTransform.getRotateInstance(Math.toRadians(-45)));

        int y = height / 4;

        // calculate the rectangle where the label is rendered
        FontRenderContext frc = new FontRenderContext(g.getTransform(), true, true);
        int maxLabelWidth = calculateMaxLabelWidth(y);
        g.setFont(getFont(maxLabelWidth, frc));
        Rectangle2D labelBounds = g.getFont().getStringBounds(label, frc);

        // draw the ribbon
        g.setColor(ribbonColor);
        g.fillRect(-width, y, width * 2, (int)(labelBounds.getHeight() * 1.2));

        // draw the label
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(labelColor);

        drawString(g, label, (int) (-labelBounds.getWidth() / 2),
                (int) (y + labelBounds.getHeight()));

        g.dispose();
    }

    static int calculateMaxLabelWidth(int y) {
        return (int)Math.sqrt(Math.pow(y, 2) * 2);
    }

    Font getFont(int maxLabelWidth, FontRenderContext frc) {
        int max = 32;
        int min = 0;
        int x = max;

        for (int i = 0; i < 10; i++) {
            int m = ((max + min) / 2);
            if (m == x) {
                break;
            }

            Font font = new Font(fontName, fontStyle, m);
            Rectangle2D labelBounds = font.getStringBounds(label, frc);
            int px = (int)labelBounds.getWidth();

            if (px > maxLabelWidth) {
                max = m;
            } else {
                min = m;
            }
            x = m;
        }
        return new Font(fontName, fontStyle, x);
    }

    static void drawString(Graphics2D g, String str, int x, int y) {
        g.drawString(str, x, y);

        if (debug) {
            Rectangle2D bounds = g.getFont().getStringBounds(str,
                    new FontRenderContext(g.getTransform(), true, true));

            g.drawRect(x, y - (int)bounds.getHeight(), (int)bounds.getWidth(), (int)bounds.getHeight());
        }
    }
}
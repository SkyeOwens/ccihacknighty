package game.ui;

import game.core.Assets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Draws item/weapon icons: your PNG if it exists, otherwise a labeled tile. */
public final class Icons {
    private Icons() {}

    public static void draw(Graphics2D g, String iconPath, String name, Color border,
                            int x, int y, int size) {
        g.setColor(new Color(30, 27, 37));
        g.fillRoundRect(x, y, size, size, 10, 10);

        BufferedImage img = Assets.get(iconPath);
        if (img != null) {
            int pad = size / 10;
            g.drawImage(img, x + pad, y + pad, size - 2 * pad, size - 2 * pad, null);
        } else {
            // Fallback: initials of the name, e.g. "Goat Skull" -> "GS"
            String initials = initials(name);
            g.setFont(UiTheme.BODY_BOLD.deriveFont((float) Math.max(11, size * 0.36)));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(border);
            int ty = y + (size + fm.getAscent() - fm.getDescent()) / 2;
            UiTheme.drawCentered(g, initials, x + size / 2.0, ty);
        }

        g.setColor(border);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, size, size, 10, 10);
    }

    private static String initials(String name) {
        String[] words = name.split(" ");
        if (words.length >= 2) {
            return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
        }
        return name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
    }
}

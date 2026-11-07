package game.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Colors, fonts and small drawing helpers shared by every screen. Restyle the game here. */
public final class UiTheme {
    private UiTheme() {}

    // ---------------- Colors ----------------
    public static final Color BG           = new Color(27, 24, 33);
    public static final Color PANEL        = new Color(43, 38, 53);
    public static final Color PANEL_LIGHT  = new Color(58, 52, 71);
    public static final Color BORDER       = new Color(88, 80, 106);
    public static final Color TEXT         = new Color(238, 234, 244);
    public static final Color TEXT_DIM     = new Color(165, 158, 182);
    public static final Color ACCENT       = new Color(255, 196, 64);
    public static final Color GOOD         = new Color(115, 220, 110);
    public static final Color BAD          = new Color(235, 92, 92);
    public static final Color HP_RED       = new Color(214, 58, 58);
    public static final Color XP_GREEN     = new Color(96, 196, 90);
    public static final Color MATERIAL     = new Color(120, 220, 90);
    public static final Color ARENA        = new Color(181, 160, 118);
    public static final Color ARENA_LINE   = new Color(167, 146, 104);
    public static final Color ARENA_WALL   = new Color(92, 74, 52);

    // ---------------- Fonts ----------------
    public static final Font TITLE     = new Font(Font.SANS_SERIF, Font.BOLD, 64);
    public static final Font HEADER    = new Font(Font.SANS_SERIF, Font.BOLD, 30);
    public static final Font SUBHEADER = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    public static final Font BODY      = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    public static final Font BODY_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font SMALL     = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font SMALL_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font HUD_BIG   = new Font(Font.SANS_SERIF, Font.BOLD, 42);
    public static final Font DAMAGE    = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    public static final Font DAMAGE_BIG = new Font(Font.SANS_SERIF, Font.BOLD, 20);

    /** Brotato-style rarity colors: white, blue, purple, red. */
    public static Color tierColor(int tier) {
        switch (tier) {
            case 2:  return new Color(80, 150, 255);
            case 3:  return new Color(178, 92, 255);
            case 4:  return new Color(255, 78, 78);
            default: return new Color(205, 205, 205);
        }
    }

    public static Color withAlpha(Color c, double alpha) {
        int a = (int) Math.max(0, Math.min(255, alpha * 255));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    // ---------------- Drawing helpers ----------------

    public static void drawCentered(Graphics2D g, String text, double centerX, double baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (float) (centerX - fm.stringWidth(text) / 2.0), (float) baselineY);
    }

    public static void drawRight(Graphics2D g, String text, double rightX, double baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (float) (rightX - fm.stringWidth(text)), (float) baselineY);
    }

    /** Text with a dark outline so it reads on any background. */
    public static void drawOutlined(Graphics2D g, String text, double x, double y, Color fill) {
        Color old = g.getColor();
        g.setColor(withAlpha(Color.BLACK, fill.getAlpha() / 255.0 * 0.85));
        for (int dx = -2; dx <= 2; dx += 2) {
            for (int dy = -2; dy <= 2; dy += 2) {
                if (dx != 0 || dy != 0) g.drawString(text, (float) x + dx, (float) y + dy);
            }
        }
        g.setColor(fill);
        g.drawString(text, (float) x, (float) y);
        g.setColor(old);
    }

    public static void drawOutlinedCentered(Graphics2D g, String text, double cx, double y, Color fill) {
        FontMetrics fm = g.getFontMetrics();
        drawOutlined(g, text, cx - fm.stringWidth(text) / 2.0, y, fill);
    }

    public static void drawPanel(Graphics2D g, double x, double y, double w, double h,
                                 Color fill, Color border, float borderWidth) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 14, 14);
        g.setColor(fill);
        g.fill(r);
        if (border != null) {
            g.setColor(border);
            g.setStroke(new BasicStroke(borderWidth));
            g.draw(r);
        }
    }

    public static void drawPanel(Graphics2D g, double x, double y, double w, double h) {
        drawPanel(g, x, y, w, h, PANEL, BORDER, 2f);
    }

    /** The green material blob used in the HUD and shop. */
    public static void drawMaterialIcon(Graphics2D g, double cx, double cy, double r) {
        g.setColor(MATERIAL.darker());
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setColor(MATERIAL);
        g.fill(new Ellipse2D.Double(cx - r * 0.8, cy - r * 0.9, r * 1.6, r * 1.6));
        g.setColor(withAlpha(Color.WHITE, 0.6));
        g.fill(new Ellipse2D.Double(cx - r * 0.45, cy - r * 0.6, r * 0.5, r * 0.4));
    }

    /** A bar like the HP bar: background, fill and centered label. */
    public static void drawBar(Graphics2D g, int x, int y, int w, int h, double fraction,
                               Color fill, String label) {
        fraction = Math.max(0, Math.min(1, fraction));
        g.setColor(new Color(20, 18, 24));
        g.fillRoundRect(x - 2, y - 2, w + 4, h + 4, 8, 8);
        g.setColor(new Color(60, 54, 70));
        g.fillRoundRect(x, y, w, h, 6, 6);
        g.setColor(fill);
        g.fillRoundRect(x, y, (int) (w * fraction), h, 6, 6);
        if (label != null) {
            g.setFont(SMALL_BOLD);
            FontMetrics fm = g.getFontMetrics();
            int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
            drawOutlinedCentered(g, label, x + w / 2.0, ty, TEXT);
        }
    }

    /** Splits text into lines that fit inside maxWidth pixels. */
    public static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String attempt = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(attempt) > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(attempt);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    /** Picks a color for a stat line: green if it starts with "+", red if "-". */
    public static Color modifierColor(String line) {
        if (line.startsWith("+")) return GOOD;
        if (line.startsWith("-")) return BAD;
        return TEXT;
    }

    /** A floating box with a title and lines, kept inside the window. */
    public static void drawTooltip(Graphics2D g, String title, Color titleColor,
                                   List<String> lines, int mouseX, int mouseY,
                                   int screenW, int screenH) {
        int w = 240;
        g.setFont(BODY);
        FontMetrics fm = g.getFontMetrics();
        List<String> wrapped = new ArrayList<>();
        for (String l : lines) wrapped.addAll(wrap(l, fm, w - 24));
        int h = 44 + wrapped.size() * 19;
        int x = Math.min(mouseX + 16, screenW - w - 8);
        int y = Math.min(mouseY + 16, screenH - h - 8);
        drawPanel(g, x, y, w, h, new Color(22, 20, 28, 240), titleColor, 2f);
        g.setFont(BODY_BOLD);
        g.setColor(titleColor);
        g.drawString(title, x + 12, y + 24);
        g.setFont(BODY);
        int ly = y + 46;
        for (String l : wrapped) {
            g.setColor(modifierColor(l));
            g.drawString(l, x + 12, ly);
            ly += 19;
        }
    }
}

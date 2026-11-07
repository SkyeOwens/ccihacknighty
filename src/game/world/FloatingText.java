package game.world;

import java.awt.Color;

/** Damage numbers, "DODGE", "+1" and similar popups. */
public class FloatingText {
    public static final double LIFETIME = 0.8;

    double x, y;
    final String text;
    final Color color;
    final boolean big;
    double life = LIFETIME;

    FloatingText(String text, double x, double y, Color color, boolean big) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.big = big;
    }

    public String getText()  { return text; }
    public double getX()     { return x; }
    public double getY()     { return y; }
    public Color getColor()  { return color; }
    public boolean isBig()   { return big; }
    /** 1 when new, fading to 0. */
    public double getAlpha() { return Math.max(0, Math.min(1, life / LIFETIME * 1.5)); }
}

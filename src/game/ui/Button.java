package game.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** A simple clickable rectangle with a label. */
public class Button {
    public int x, y, w, h;
    public String label;
    public boolean enabled = true;
    public Color color = UiTheme.ACCENT;
    public Color textColor = new Color(30, 24, 20);

    public Button(int x, int y, int w, int h, String label) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.label = label;
    }

    public boolean contains(int px, int py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    /** True if enabled and the point is inside: use this in onClick. */
    public boolean clicked(int px, int py) {
        return enabled && contains(px, py);
    }

    public void draw(Graphics2D g, int mouseX, int mouseY) {
        boolean hover = enabled && contains(mouseX, mouseY);
        Color fill = !enabled ? new Color(78, 72, 88) : hover ? color.brighter() : color;
        UiTheme.drawPanel(g, x, y + 3, w, h, fill.darker().darker(), null, 0);  // shadow
        UiTheme.drawPanel(g, x, y + (hover ? 1 : 0), w, h, fill, null, 0);
        g.setFont(UiTheme.BODY_BOLD);
        g.setColor(enabled ? textColor : new Color(150, 144, 160));
        FontMetrics fm = g.getFontMetrics();
        int ty = y + (hover ? 1 : 0) + (h + fm.getAscent() - fm.getDescent()) / 2;
        UiTheme.drawCentered(g, label, x + w / 2.0, ty);
    }
}

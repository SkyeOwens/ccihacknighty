package game.ui;

import game.GameSession;
import game.Main;
import game.core.GamePanel;
import game.core.Screen;
import game.weapons.WeaponDef;
import game.weapons.Weapons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Title screen: pick a starting weapon to begin a run. */
public class TitleScreen implements Screen {
    private static final int CARD_W = 250, CARD_H = 140, GAP = 24, COLS = 3;
    private static final int GRID_X = (GamePanel.WIDTH - (COLS * CARD_W + (COLS - 1) * GAP)) / 2;
    private static final int GRID_Y = 280;

    private final GamePanel game;
    private double time;

    public TitleScreen(GamePanel game) {
        this.game = game;
    }

    @Override
    public void update(double dt) {
        time += dt;
    }

    private int cardX(int i) { return GRID_X + (i % COLS) * (CARD_W + GAP); }
    private int cardY(int i) { return GRID_Y + (i / COLS) * (CARD_H + GAP); }

    private boolean inCard(int i, int x, int y) {
        return x >= cardX(i) && x <= cardX(i) + CARD_W && y >= cardY(i) && y <= cardY(i) + CARD_H;
    }

    @Override
    public void onClick(int x, int y) {
        for (int i = 0; i < Weapons.ALL.size(); i++) {
            if (inCard(i, x, y)) {
                game.startRun(Weapons.ALL.get(i));
                return;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        int mx = game.getInput().getMouseX(), my = game.getInput().getMouseY();
        g.setColor(UiTheme.BG);
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // Title with a gentle bob
        double bob = Math.sin(time * 2) * 4;
        g.setFont(UiTheme.TITLE);
        UiTheme.drawOutlinedCentered(g, Main.GAME_TITLE.toUpperCase(),
                GamePanel.WIDTH / 2.0, 130 + bob, UiTheme.ACCENT);
        g.setFont(UiTheme.SUBHEADER);
        g.setColor(UiTheme.TEXT_DIM);
        UiTheme.drawCentered(g, "Survive " + GameSession.FINAL_WAVE + " waves until help arrives",
                GamePanel.WIDTH / 2.0, 180);

        g.setFont(UiTheme.HEADER);
        g.setColor(UiTheme.TEXT);
        UiTheme.drawCentered(g, "Choose your starting weapon", GamePanel.WIDTH / 2.0, 250);

        for (int i = 0; i < Weapons.ALL.size(); i++) {
            drawCard(g, Weapons.ALL.get(i), cardX(i), cardY(i), inCard(i, mx, my));
        }

        g.setFont(UiTheme.BODY);
        g.setColor(UiTheme.TEXT_DIM);
        UiTheme.drawCentered(g, "WASD / Arrow keys to move     Weapons fire automatically     Esc to pause",
                GamePanel.WIDTH / 2.0, 670);
    }

    private void drawCard(Graphics2D g, WeaponDef w, int x, int y, boolean hover) {
        Color border = hover ? UiTheme.ACCENT : UiTheme.BORDER;
        UiTheme.drawPanel(g, x, y - (hover ? 3 : 0), CARD_W, CARD_H,
                hover ? UiTheme.PANEL_LIGHT : UiTheme.PANEL, border, hover ? 3f : 2f);
        int oy = hover ? -3 : 0;
        Icons.draw(g, w.getIconPath(), w.getName(), w.getColor(), x + 16, y + 16 + oy, 56);

        g.setFont(UiTheme.SUBHEADER);
        g.setColor(UiTheme.TEXT);
        g.drawString(w.getName(), x + 86, y + 42 + oy);
        g.setFont(UiTheme.SMALL);
        g.setColor(UiTheme.TEXT_DIM);
        g.drawString(w.isMelee() ? "Melee" : "Ranged", x + 86, y + 64 + oy);

        List<String> lines = w.getDescriptionLines();
        g.setFont(UiTheme.SMALL);
        g.setColor(UiTheme.TEXT);
        int ly = y + 96 + oy;
        for (int i = 1; i < lines.size() && i <= 3; i++) {   // skip "Melee weapon"
            g.drawString(lines.get(i), x + 16, ly);
            ly += 17;
        }
        if (hover) {
            g.setColor(UiTheme.ACCENT);
            g.setStroke(new BasicStroke(2f));
            g.setFont(UiTheme.SMALL_BOLD);
            UiTheme.drawRight(g, "Click to start", x + CARD_W - 14, y + CARD_H - 12 + oy);
        }
    }
}

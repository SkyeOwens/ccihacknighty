package game.ui;

import game.GameSession;
import game.core.GamePanel;
import game.core.Screen;
import game.items.Item;
import game.items.Player;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Shown when the run ends, by winning or by dying. */
public class EndScreen implements Screen {
    private final GamePanel game;
    private final GameSession session;
    private final boolean victory;

    private final Button againButton = new Button(GamePanel.WIDTH / 2 - 250, 600, 240, 54, "Play again");
    private final Button quitButton = new Button(GamePanel.WIDTH / 2 + 10, 600, 240, 54, "Quit");

    public EndScreen(GamePanel game, GameSession session, boolean victory) {
        this.game = game;
        this.session = session;
        this.victory = victory;
        quitButton.color = new Color(150, 140, 165);
    }

    @Override
    public void update(double dt) {}

    @Override
    public void onClick(int x, int y) {
        if (againButton.clicked(x, y)) game.showTitle();
        else if (quitButton.clicked(x, y)) System.exit(0);
    }

    @Override
    public void render(Graphics2D g) {
        int mx = game.getInput().getMouseX(), my = game.getInput().getMouseY();
        Player p = session.getPlayer();
        double cx = GamePanel.WIDTH / 2.0;

        g.setColor(UiTheme.BG);
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g.setFont(UiTheme.TITLE);
        UiTheme.drawOutlinedCentered(g, victory ? "HELP HAS ARRIVED!" : "YOU DIED", cx, 130,
                victory ? UiTheme.GOOD : UiTheme.BAD);
        g.setFont(UiTheme.SUBHEADER);
        g.setColor(UiTheme.TEXT_DIM);
        UiTheme.drawCentered(g, victory
                ? "You survived all " + GameSession.FINAL_WAVE + " waves."
                : "You fell on wave " + session.getWave() + " of " + GameSession.FINAL_WAVE + ".", cx, 175);

        // Run summary
        int pw = 460, px = (int) cx - pw / 2, py = 215;
        UiTheme.drawPanel(g, px, py, pw, 210);
        String[][] rows = {
                { "Level reached",        String.valueOf(p.getLevel()) },
                { "Enemies killed",       String.valueOf(session.getKills()) },
                { "Materials collected",  String.valueOf(session.getMaterialsCollected()) },
                { "Items",                String.valueOf(p.getInventory().size()) },
                { "Weapons",              String.valueOf(p.getWeapons().size()) },
        };
        int y = py + 42;
        for (String[] row : rows) {
            g.setFont(UiTheme.BODY);
            g.setColor(UiTheme.TEXT);
            g.drawString(row[0], px + 24, y);
            g.setFont(UiTheme.BODY_BOLD);
            g.setColor(UiTheme.ACCENT);
            UiTheme.drawRight(g, row[1], px + pw - 24, y);
            y += 36;
        }

        // Build: every unique item owned
        List<Item> items = p.getUniqueItems();
        int size = 40, gap = 6;
        int total = items.size() * (size + gap) - gap;
        int ix = (int) cx - total / 2;
        for (Item item : items) {
            Icons.draw(g, item.getIconPath(), item.getName(), UiTheme.tierColor(item.getTier()), ix, 470, size);
            int count = p.countOf(item);
            if (count > 1) {
                g.setFont(UiTheme.SMALL_BOLD);
                UiTheme.drawOutlined(g, "x" + count, ix + size - 18, 470 + size - 3, UiTheme.TEXT);
            }
            ix += size + gap;
        }

        againButton.draw(g, mx, my);
        quitButton.draw(g, mx, my);
    }
}

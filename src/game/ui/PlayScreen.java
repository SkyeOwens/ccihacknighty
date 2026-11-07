package game.ui;

import game.GameSession;
import game.core.GamePanel;
import game.core.Input;
import game.core.Screen;
import game.items.Player;
import game.world.World;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/** The arena during a wave, plus the HUD and pause menu. */
public class PlayScreen implements Screen {
    private static final double END_DELAY = 1.6; // seconds to show "Wave complete" / "You died"

    private final GamePanel game;
    private final GameSession session;
    private final World world;
    private boolean paused;
    private double endTimer = -1;

    private final Button resumeButton = new Button(GamePanel.WIDTH / 2 - 120, 340, 240, 50, "Resume");
    private final Button quitButton = new Button(GamePanel.WIDTH / 2 - 120, 405, 240, 50, "Quit to menu");

    public PlayScreen(GamePanel game, GameSession session) {
        this.game = game;
        this.session = session;
        this.world = new World(session);
        quitButton.color = new Color(150, 140, 165);
    }

    public World getWorld() { return world; }

    @Override
    public void update(double dt) {
        if (paused) return;

        boolean finished = world.isWaveOver() || world.getPlayer().isDead();
        if (!finished) {
            Input in = game.getInput();
            world.update(dt, in.getMoveX(), in.getMoveY());
            return;
        }

        // Short pause so the player sees what happened before the screen changes.
        if (endTimer < 0) endTimer = END_DELAY;
        endTimer -= dt;
        if (endTimer <= 0) {
            if (world.getPlayer().isDead()) {
                game.showEnd(false);
            } else {
                game.onWaveComplete();
            }
        }
    }

    @Override
    public void onKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_P) {
            paused = !paused;
        }
    }

    @Override
    public void onClick(int x, int y) {
        if (!paused) return;
        if (resumeButton.clicked(x, y)) paused = false;
        else if (quitButton.clicked(x, y)) game.showTitle();
    }

    @Override
    public void render(Graphics2D g) {
        WorldRenderer.render(g, world);
        drawHud(g);
        drawBanner(g);
        if (paused) drawPause(g);
    }

    // ---------------- HUD ----------------

    private void drawHud(Graphics2D g) {
        Player p = world.getPlayer();

        // HP
        String hp = "HP  " + (int) Math.ceil(p.getCurrentHp()) + " / " + p.getMaxHp();
        UiTheme.drawBar(g, 40, 38, 260, 26, p.getCurrentHp() / p.getMaxHp(), UiTheme.HP_RED, hp);

        // XP + level
        UiTheme.drawBar(g, 40, 72, 260, 10, p.getXp() / (double) p.getXpToNextLevel(),
                UiTheme.XP_GREEN, null);
        g.setFont(UiTheme.SMALL_BOLD);
        UiTheme.drawOutlined(g, "LV " + p.getLevel(), 310, 82, UiTheme.TEXT);

        // Materials
        UiTheme.drawMaterialIcon(g, 52, 106, 9);
        g.setFont(UiTheme.SUBHEADER);
        UiTheme.drawOutlined(g, String.valueOf(p.getMaterials()), 68, 114, UiTheme.TEXT);

        // Wave + timer
        g.setFont(UiTheme.SUBHEADER);
        UiTheme.drawOutlinedCentered(g, "WAVE " + world.getWave() + " / " + GameSession.FINAL_WAVE,
                GamePanel.WIDTH / 2.0, 52, UiTheme.TEXT);
        int secs = (int) Math.ceil(world.getTimeLeft());
        g.setFont(UiTheme.HUD_BIG);
        UiTheme.drawOutlinedCentered(g, String.valueOf(secs), GamePanel.WIDTH / 2.0, 100,
                secs <= 5 ? UiTheme.BAD : UiTheme.TEXT);

        g.setFont(UiTheme.SMALL);
        UiTheme.drawOutlined(g, "Esc: pause", GamePanel.WIDTH - 110, 52, UiTheme.withAlpha(UiTheme.TEXT, 0.8));
    }

    /** "WAVE 3" at the start, "WAVE COMPLETE" / "YOU DIED" at the end. */
    private void drawBanner(Graphics2D g) {
        String text = null, sub = null;
        Color color = UiTheme.TEXT;
        double alpha = 1;

        if (world.getPlayer().isDead()) {
            text = "YOU DIED";
            color = UiTheme.BAD;
        } else if (world.isWaveOver()) {
            text = session.isFinalWave() ? "YOU SURVIVED!" : "WAVE COMPLETE";
            color = UiTheme.GOOD;
            if (world.getHarvestedThisWave() > 0) sub = "+" + world.getHarvestedThisWave() + " materials from Harvesting";
        } else if (world.getElapsed() < 2.0) {
            text = "WAVE " + world.getWave();
            sub = session.isFinalWave() ? "Final wave! Help is almost here." : "Survive!";
            alpha = Math.min(1, (2.0 - world.getElapsed()) * 1.5);
        }
        if (text == null) return;

        if (world.isWaveOver() || world.getPlayer().isDead()) {
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        }
        g.setFont(UiTheme.TITLE);
        UiTheme.drawOutlinedCentered(g, text, GamePanel.WIDTH / 2.0, 330, UiTheme.withAlpha(color, alpha));
        if (sub != null) {
            g.setFont(UiTheme.SUBHEADER);
            UiTheme.drawOutlinedCentered(g, sub, GamePanel.WIDTH / 2.0, 375, UiTheme.withAlpha(UiTheme.TEXT, alpha));
        }
    }

    private void drawPause(Graphics2D g) {
        int mx = game.getInput().getMouseX(), my = game.getInput().getMouseY();
        g.setColor(new Color(10, 8, 14, 170));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        g.setFont(UiTheme.TITLE);
        UiTheme.drawOutlinedCentered(g, "PAUSED", GamePanel.WIDTH / 2.0, 290, UiTheme.TEXT);
        resumeButton.draw(g, mx, my);
        quitButton.draw(g, mx, my);
    }
}

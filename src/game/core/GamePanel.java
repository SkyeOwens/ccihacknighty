package game.core;

import game.GameSession;
import game.ui.EndScreen;
import game.ui.PlayScreen;
import game.ui.ShopScreen;
import game.ui.TitleScreen;
import game.ui.UiTheme;
import game.weapons.WeaponDef;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The game window's content. Runs the game loop (~60 times per second) and
 * switches between screens. Everything runs on Swing's event thread, so there
 * are no threading problems between input, updates and drawing.
 */
public class GamePanel extends JPanel {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    private final Input input = new Input();
    private Screen screen;
    private GameSession session;
    private Timer timer;
    private long lastTime;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(UiTheme.BG);
        setFocusable(true);
        setDoubleBuffered(true);

        addKeyListener(input.keyListener);
        addMouseMotionListener(input.mouseMotionListener);
        addFocusListener(input.focusListener);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (screen != null) screen.onKeyPressed(e.getKeyCode());
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (e.getButton() == MouseEvent.BUTTON1 && screen != null) {
                    screen.onClick(e.getX(), e.getY());
                }
            }
        });

        showTitle();
    }

    /** Starts the game loop. */
    public void start() {
        lastTime = System.nanoTime();
        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
        timer.start();
        requestFocusInWindow();
    }

    private void tick() {
        long now = System.nanoTime();
        double dt = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;
        dt = Math.min(dt, 0.05); // avoid huge jumps if the window freezes
        if (screen != null) screen.update(dt);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (screen != null) screen.render(g2);
        g2.dispose();
    }

    // =====================================================================
    // Screen flow:  Title -> Play -> Shop -> Play -> ... -> End -> Title
    // =====================================================================

    public void showTitle() {
        session = null;
        setScreen(new TitleScreen(this));
    }

    public void startRun(WeaponDef startingWeapon) {
        session = new GameSession(startingWeapon);
        setScreen(new PlayScreen(this, session));
    }

    /** Called by PlayScreen when the wave timer runs out. */
    public void onWaveComplete() {
        if (session.isFinalWave()) {
            showEnd(true);
        } else {
            session.openShop();
            setScreen(new ShopScreen(this, session));
        }
    }

    /** Called by ShopScreen's "Start wave" button. */
    public void startNextWave() {
        session.advanceWave();
        setScreen(new PlayScreen(this, session));
    }

    public void showEnd(boolean victory) {
        setScreen(new EndScreen(this, session, victory));
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen()       { return screen; }
    public Input getInput()         { return input; }
    public GameSession getSession() { return session; }
}

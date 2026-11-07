package game;

import game.core.GamePanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Start the game from here. */
public class Main {
    public static final String GAME_TITLE = "Brotato Lite";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame(GAME_TITLE);
                GamePanel panel = new GamePanel();
                frame.add(panel);
                frame.pack();
                frame.setResizable(false);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                panel.start();
            }
        });
    }
}

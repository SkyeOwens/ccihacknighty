package game.core;

import java.awt.Graphics2D;

/** One screen of the game (title, playing, shop, game over). GamePanel shows one at a time. */
public interface Screen {
    void update(double dt);

    void render(Graphics2D g);

    /** Left mouse button pressed at (x, y). */
    default void onClick(int x, int y) {}

    /** A key was pressed (KeyEvent.VK_... code). */
    default void onKeyPressed(int keyCode) {}
}

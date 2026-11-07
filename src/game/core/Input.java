package game.core;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
import java.util.Set;

/** Remembers which keys are held down and where the mouse is. */
public class Input {
    private final Set<Integer> held = new HashSet<>();
    private int mouseX = -1, mouseY = -1;

    final KeyAdapter keyListener = new KeyAdapter() {
        @Override public void keyPressed(KeyEvent e)  { held.add(e.getKeyCode()); }
        @Override public void keyReleased(KeyEvent e) { held.remove(e.getKeyCode()); }
    };

    final MouseMotionAdapter mouseMotionListener = new MouseMotionAdapter() {
        @Override public void mouseMoved(MouseEvent e)   { mouseX = e.getX(); mouseY = e.getY(); }
        @Override public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    };

    /** Clears held keys if the window loses focus, so the player doesn't keep walking. */
    final FocusAdapter focusListener = new FocusAdapter() {
        @Override public void focusLost(FocusEvent e) { held.clear(); }
    };

    public boolean isHeld(int keyCode) {
        return held.contains(keyCode);
    }

    /** -1 for left, 1 for right, 0 for neither (WASD or arrow keys). */
    public double getMoveX() {
        double x = 0;
        if (isHeld(KeyEvent.VK_A) || isHeld(KeyEvent.VK_LEFT))  x -= 1;
        if (isHeld(KeyEvent.VK_D) || isHeld(KeyEvent.VK_RIGHT)) x += 1;
        return x;
    }

    /** -1 for up, 1 for down, 0 for neither. */
    public double getMoveY() {
        double y = 0;
        if (isHeld(KeyEvent.VK_W) || isHeld(KeyEvent.VK_UP))   y -= 1;
        if (isHeld(KeyEvent.VK_S) || isHeld(KeyEvent.VK_DOWN)) y += 1;
        return y;
    }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
}

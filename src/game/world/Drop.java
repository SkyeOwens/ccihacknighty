package game.world;

/** Something on the floor to pick up: materials, or a crate that contains an item. */
public class Drop {
    double x, y;
    final int value;
    final boolean crate;
    boolean collected;
    double speed;   // grows while being pulled toward the player

    Drop(double x, double y, int value, boolean crate) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.crate = crate;
    }

    public double getX()     { return x; }
    public double getY()     { return y; }
    public boolean isCrate() { return crate; }
}

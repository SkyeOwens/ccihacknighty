package game.world;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class Projectile {
    public static final double RADIUS = 5;

    double x, y;
    final double vx, vy;
    final int damage;
    final boolean crit;
    int pierceLeft;
    double distanceLeft;
    final Color color;
    final Set<Enemy> alreadyHit = new HashSet<>();
    boolean dead;

    Projectile(double x, double y, double angle, double speed, int damage, boolean crit,
               int pierce, double maxDistance, Color color) {
        this.x = x;
        this.y = y;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.damage = damage;
        this.crit = crit;
        this.pierceLeft = pierce;
        this.distanceLeft = maxDistance;
        this.color = color;
    }

    public double getX()     { return x; }
    public double getY()     { return y; }
    public double getVx()    { return vx; }
    public double getVy()    { return vy; }
    public Color getColor()  { return color; }
}

package game.ui;

import game.core.Assets;
import game.items.Player;
import game.weapons.Weapon;
import game.weapons.WeaponDef;
import game.world.Drop;
import game.world.Enemy;
import game.world.EnemyType;
import game.world.FloatingText;
import game.world.Projectile;
import game.world.SpawnMarker;
import game.world.World;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Draws the arena and everything in it. If you add PNGs to the assets folder
 * (see Assets.java) they replace the built-in shapes automatically.
 */
public final class WorldRenderer {
    private WorldRenderer() {}

    private static final int TILE = 64;

    public static void render(Graphics2D g, World world) {
        drawArena(g);
        for (SpawnMarker m : world.getMarkers()) drawMarker(g, m);
        for (Drop d : world.getDrops()) drawDrop(g, d);
        for (Enemy e : world.getEnemies()) drawEnemy(g, e, world.getPlayer());
        drawPlayer(g, world);
        for (Projectile p : world.getProjectiles()) drawProjectile(g, p);
        for (FloatingText t : world.getTexts()) drawText(g, t);
    }

    // ---------------- Arena ----------------

    private static void drawArena(Graphics2D g) {
        int w = (int) World.WIDTH, h = (int) World.HEIGHT, m = (int) World.MARGIN;
        g.setColor(UiTheme.ARENA_WALL);
        g.fillRect(0, 0, w, h);
        g.setColor(UiTheme.ARENA);
        g.fillRect(m, m, w - 2 * m, h - 2 * m);
        g.setColor(UiTheme.ARENA_LINE);
        g.setStroke(new BasicStroke(1f));
        for (int x = m + TILE; x < w - m; x += TILE) g.drawLine(x, m, x, h - m);
        for (int y = m + TILE; y < h - m; y += TILE) g.drawLine(m, y, w - m, y);
        g.setColor(new Color(0, 0, 0, 60));
        g.setStroke(new BasicStroke(4f));
        g.drawRect(m, m, w - 2 * m, h - 2 * m);
    }

    // ---------------- Spawn warnings ----------------

    private static void drawMarker(Graphics2D g, SpawnMarker m) {
        double p = m.getProgress();
        double size = m.getType() == EnemyType.BOSS ? 40 : 12;
        // Blink faster as the spawn gets closer.
        double blink = 0.5 + 0.5 * Math.sin(p * p * 40);
        g.setColor(UiTheme.withAlpha(new Color(220, 40, 40), 0.45 + 0.5 * blink));
        g.setStroke(new BasicStroke(m.getType() == EnemyType.BOSS ? 7f : 4f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(m.getX() - size, m.getY() - size, m.getX() + size, m.getY() + size));
        g.draw(new Line2D.Double(m.getX() - size, m.getY() + size, m.getX() + size, m.getY() - size));
    }

    // ---------------- Drops ----------------

    private static void drawDrop(Graphics2D g, Drop d) {
        if (d.isCrate()) {
            BufferedImage img = Assets.get("crate.png");
            if (img != null) {
                g.drawImage(img, (int) d.getX() - 14, (int) d.getY() - 14, 28, 28, null);
                return;
            }
            g.setColor(new Color(130, 85, 45));
            g.fillRect((int) d.getX() - 12, (int) d.getY() - 12, 24, 24);
            g.setColor(new Color(80, 50, 25));
            g.setStroke(new BasicStroke(2f));
            g.drawRect((int) d.getX() - 12, (int) d.getY() - 12, 24, 24);
            g.drawLine((int) d.getX() - 12, (int) d.getY(), (int) d.getX() + 12, (int) d.getY());
        } else {
            BufferedImage img = Assets.get("material.png");
            if (img != null) {
                g.drawImage(img, (int) d.getX() - 7, (int) d.getY() - 7, 14, 14, null);
            } else {
                UiTheme.drawMaterialIcon(g, d.getX(), d.getY(), 6);
            }
        }
    }

    // ---------------- Enemies ----------------

    private static void drawEnemy(Graphics2D g, Enemy e, Player player) {
        double r = e.getRadius();
        double x = e.getX(), y = e.getY();

        // Shadow
        g.setColor(new Color(0, 0, 0, 50));
        g.fill(new Ellipse2D.Double(x - r, y + r * 0.55, r * 2, r * 0.7));

        BufferedImage img = Assets.get(e.getType().getIconPath());
        if (img != null) {
            boolean faceLeft = player.getX() < x;
            int size = (int) (r * 2.4);
            if (faceLeft) {
                g.drawImage(img, (int) (x + size / 2), (int) (y - size / 2), -size, size, null);
            } else {
                g.drawImage(img, (int) (x - size / 2), (int) (y - size / 2), size, size, null);
            }
            if (e.isFlashing()) {
                g.setColor(new Color(255, 255, 255, 140));
                g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
            }
        } else {
            Color body = e.isFlashing() ? Color.WHITE : e.getType().getColor();
            g.setColor(body.darker());
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
            g.setColor(body);
            g.fill(new Ellipse2D.Double(x - r * 0.9, y - r * 0.95, r * 1.8, r * 1.75));

            // Eyes look at the player
            double ang = Math.atan2(player.getY() - y, player.getX() - x);
            double ex = Math.cos(ang) * r * 0.25, ey = Math.sin(ang) * r * 0.2;
            double eyeR = Math.max(3, r * 0.22);
            for (int side = -1; side <= 1; side += 2) {
                double cx = x + side * r * 0.35 + ex, cy = y - r * 0.2 + ey;
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(cx - eyeR, cy - eyeR, eyeR * 2, eyeR * 2));
                g.setColor(new Color(25, 20, 30));
                double pr = eyeR * 0.55;
                g.fill(new Ellipse2D.Double(cx - pr + ex * 0.4, cy - pr + ey * 0.4, pr * 2, pr * 2));
            }
        }

        // Boss gets an HP bar.
        if (e.getType() == EnemyType.BOSS) {
            UiTheme.drawBar(g, (int) (x - 60), (int) (y - r - 22), 120, 10,
                    e.getHp() / e.getMaxHp(), UiTheme.HP_RED, null);
        }
    }

    // ---------------- Player ----------------

    private static void drawPlayer(Graphics2D g, World world) {
        Player p = world.getPlayer();
        double x = p.getX(), y = p.getY(), r = Player.RADIUS;

        // Flicker while invulnerable.
        boolean visible = !p.isInvulnerable() || ((int) (world.getElapsed() * 20)) % 2 == 0;

        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new Ellipse2D.Double(x - r, y + r * 0.6, r * 2, r * 0.7));

        if (visible) {
            BufferedImage img = Assets.get("player.png");
            if (img != null) {
                int size = (int) (r * 2.6);
                g.drawImage(img, (int) (x - size / 2), (int) (y - size / 2), size, size, null);
            } else {
                drawPotato(g, x, y, r);
            }
        }

        List<Weapon> weapons = p.getWeapons();
        for (int i = 0; i < weapons.size(); i++) {
            double[] slot = World.weaponSlotPosition(x, y, i, weapons.size());
            drawWeapon(g, weapons.get(i), slot[0], slot[1]);
        }
    }

    private static void drawPotato(Graphics2D g, double x, double y, double r) {
        Color skin = new Color(214, 170, 104);
        g.setColor(skin.darker());
        g.fill(new Ellipse2D.Double(x - r, y - r * 1.15, r * 2, r * 2.3));
        g.setColor(skin);
        g.fill(new Ellipse2D.Double(x - r * 0.9, y - r * 1.1, r * 1.8, r * 2.1));
        // Spots
        g.setColor(new Color(176, 132, 74));
        g.fill(new Ellipse2D.Double(x - r * 0.6, y + r * 0.35, 5, 4));
        g.fill(new Ellipse2D.Double(x + r * 0.35, y + r * 0.55, 4, 3));
        // Eyes
        for (int side = -1; side <= 1; side += 2) {
            double cx = x + side * r * 0.35, cy = y - r * 0.3;
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(cx - 5, cy - 6, 10, 12));
            g.setColor(new Color(25, 20, 30));
            g.fill(new Ellipse2D.Double(cx - 2.5, cy - 3, 5, 6));
        }
        // Smile
        g.setColor(new Color(90, 50, 30));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc((int) (x - 6), (int) (y + 1), 12, 8, 200, 140);
    }

    private static void drawWeapon(Graphics2D g, Weapon w, double sx, double sy) {
        WeaponDef def = w.getDef();
        AffineTransform old = g.getTransform();
        g.translate(sx, sy);
        g.rotate(w.getAimAngle());

        double thrust = def.isMelee() ? w.getThrustProgress() * w.getThrustLength() : 0;
        BufferedImage img = Assets.get(def.getIconPath());
        if (img != null) {
            g.drawImage(img, (int) thrust - 4, -12, 24, 24, null);
        } else {
            g.setColor(new Color(30, 26, 34));
            if (def.isMelee()) {
                // Handle + blade that stabs outward
                g.fill(new RoundRectangle2D.Double(thrust - 6, -3.5, 12, 7, 3, 3));
                g.setColor(def.getColor());
                g.fill(new RoundRectangle2D.Double(thrust + 4, -3, 16, 6, 3, 3));
            } else {
                g.fill(new RoundRectangle2D.Double(-6, -5, 22, 10, 4, 4));
                g.setColor(def.getColor());
                g.fill(new RoundRectangle2D.Double(-4, -3, 18, 6, 3, 3));
            }
        }
        // Motion trail for melee stabs
        if (def.isMelee() && thrust > 4) {
            g.setColor(UiTheme.withAlpha(Color.WHITE, 0.35 * w.getThrustProgress()));
            g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(0, 0, thrust, 0));
        }
        g.setTransform(old);
    }

    // ---------------- Bullets & text ----------------

    private static void drawProjectile(Graphics2D g, Projectile p) {
        double speed = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
        double tx = p.getVx() / speed * 10, ty = p.getVy() / speed * 10;
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(UiTheme.withAlpha(p.getColor(), 0.45));
        g.draw(new Line2D.Double(p.getX() - tx, p.getY() - ty, p.getX(), p.getY()));
        g.setColor(p.getColor());
        double r = Projectile.RADIUS;
        g.fill(new Ellipse2D.Double(p.getX() - r, p.getY() - r, r * 2, r * 2));
    }

    private static void drawText(Graphics2D g, FloatingText t) {
        g.setFont(t.isBig() ? UiTheme.DAMAGE_BIG : UiTheme.DAMAGE);
        UiTheme.drawOutlinedCentered(g, t.getText(), t.getX(), t.getY(),
                UiTheme.withAlpha(t.getColor(), t.getAlpha()));
    }
}

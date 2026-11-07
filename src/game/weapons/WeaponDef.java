package game.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * An immutable weapon definition (like Item, but for weapons). The player holds
 * Weapon objects, which point at one of these and track their own cooldown.
 */
public class WeaponDef {
    private final String id;
    private final String name;
    private final int price;
    private final boolean melee;
    private final int damage;
    private final double cooldown;        // seconds between attacks
    private final double range;           // pixels
    private final double projectileSpeed; // pixels per second (ranged only)
    private final int pierce;             // extra enemies a bullet can pass through
    private final int projectiles;        // bullets per shot (shotgun > 1)
    private final double spread;          // radians
    private final Color color;
    private final String iconPath;

    private WeaponDef(Builder b) {
        id = b.id;
        name = b.name;
        price = b.price;
        melee = b.melee;
        damage = b.damage;
        cooldown = b.cooldown;
        range = b.range;
        projectileSpeed = b.projectileSpeed;
        pierce = b.pierce;
        projectiles = b.projectiles;
        spread = b.spread;
        color = b.color;
        iconPath = "weapons/" + b.id + ".png";
    }

    public String getId()                { return id; }
    public String getName()              { return name; }
    public int getPrice()                { return price; }
    public int getSellPrice()            { return price / 2; }
    public boolean isMelee()             { return melee; }
    public int getDamage()               { return damage; }
    public double getCooldown()          { return cooldown; }
    public double getRange()             { return range; }
    public double getProjectileSpeed()   { return projectileSpeed; }
    public int getPierce()               { return pierce; }
    public int getProjectiles()          { return projectiles; }
    public double getSpread()            { return spread; }
    public Color getColor()              { return color; }
    public String getIconPath()          { return iconPath; }

    public List<String> getDescriptionLines() {
        List<String> lines = new ArrayList<>();
        lines.add(melee ? "Melee weapon" : "Ranged weapon");
        lines.add("Damage: " + damage + (projectiles > 1 ? " x" + projectiles : ""));
        lines.add(String.format("Cooldown: %.2fs", cooldown));
        lines.add("Range: " + (int) range);
        if (pierce > 0) {
            lines.add("Pierces " + pierce + " enemy" + (pierce > 1 ? "s" : ""));
        }
        return lines;
    }

    public static Builder builder(String id, String name, int price, boolean melee) {
        return new Builder(id, name, price, melee);
    }

    public static class Builder {
        private final String id;
        private final String name;
        private final int price;
        private final boolean melee;
        private int damage = 5;
        private double cooldown = 1.0;
        private double range = 150;
        private double projectileSpeed = 700;
        private int pierce = 0;
        private int projectiles = 1;
        private double spread = 0;
        private Color color = Color.LIGHT_GRAY;

        private Builder(String id, String name, int price, boolean melee) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.melee = melee;
        }

        public Builder damage(int v)          { damage = v; return this; }
        public Builder cooldown(double v)     { cooldown = v; return this; }
        public Builder range(double v)        { range = v; return this; }
        public Builder projectileSpeed(double v) { projectileSpeed = v; return this; }
        public Builder pierce(int v)          { pierce = v; return this; }
        public Builder projectiles(int v)     { projectiles = v; return this; }
        public Builder spreadDegrees(double v) { spread = Math.toRadians(v); return this; }
        public Builder color(Color v)         { color = v; return this; }

        public WeaponDef build() {
            return new WeaponDef(this);
        }
    }
}

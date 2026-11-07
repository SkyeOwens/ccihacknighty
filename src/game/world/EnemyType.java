package game.world;

import java.awt.Color;

/**
 * Enemy definitions. HP and damage grow each wave:
 *   hp     = baseHp     + hpPerWave     * (wave - 1)
 *   damage = baseDamage + damagePerWave * (wave - 1)
 */
public enum EnemyType {
    //        name            hp  hp/w  speed dmg  dmg/w radius mats first weight color
    BABY   ("Baby Alien",     8,  3.0,  100,  1,   0.30, 16,    1,   1,    10, new Color(150, 95, 205)),
    CHARGER("Charger",        5,  2.0,  160,  1,   0.25, 13,    1,   3,    5,  new Color(225, 95, 95)),
    BRUISER("Bruiser",        30, 8.0,  65,   2,   0.50, 26,    3,   5,    2,  new Color(95, 150, 95)),
    BOSS   ("Mother Alien",   500, 0,   85,   6,   0,    55,    30,  999,  0,  new Color(120, 45, 145));

    private final String displayName;
    private final double baseHp, hpPerWave, speed, baseDamage, damagePerWave, radius;
    private final int materials, firstWave, spawnWeight;
    private final Color color;

    EnemyType(String displayName, double baseHp, double hpPerWave, double speed,
              double baseDamage, double damagePerWave, double radius,
              int materials, int firstWave, int spawnWeight, Color color) {
        this.displayName = displayName;
        this.baseHp = baseHp;
        this.hpPerWave = hpPerWave;
        this.speed = speed;
        this.baseDamage = baseDamage;
        this.damagePerWave = damagePerWave;
        this.radius = radius;
        this.materials = materials;
        this.firstWave = firstWave;
        this.spawnWeight = spawnWeight;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public double getSpeed()       { return speed; }
    public double getRadius()      { return radius; }
    public int getMaterials()      { return materials; }
    public int getFirstWave()      { return firstWave; }
    public int getSpawnWeight()    { return spawnWeight; }
    public Color getColor()        { return color; }
    public String getIconPath()    { return "enemies/" + name().toLowerCase() + ".png"; }

    public double hpForWave(int wave) {
        return baseHp + hpPerWave * (wave - 1);
    }

    public int damageForWave(int wave) {
        return (int) Math.round(baseDamage + damagePerWave * (wave - 1));
    }
}

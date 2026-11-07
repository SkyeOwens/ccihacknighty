package game.world;

public class Enemy {
    final EnemyType type;
    double x, y;
    double hp;
    final double maxHp;
    final int damage;
    double hitFlash;      // > 0 right after being hit (drawn white)
    double knockX, knockY;
    boolean dead;

    Enemy(EnemyType type, double x, double y, int wave) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.maxHp = type.hpForWave(wave);
        this.hp = maxHp;
        this.damage = type.damageForWave(wave);
    }

    public EnemyType getType()  { return type; }
    public double getX()        { return x; }
    public double getY()        { return y; }
    public double getRadius()   { return type.getRadius(); }
    public double getHp()       { return hp; }
    public double getMaxHp()    { return maxHp; }
    public boolean isFlashing() { return hitFlash > 0; }
}

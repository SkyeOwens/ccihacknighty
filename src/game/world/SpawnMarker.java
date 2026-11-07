package game.world;

/** A red X that warns the player an enemy is about to appear there. */
public class SpawnMarker {
    final EnemyType type;
    final double x, y;
    final double duration;
    double timer;

    SpawnMarker(EnemyType type, double x, double y, double duration) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.duration = duration;
        this.timer = duration;
    }

    public double getX()        { return x; }
    public double getY()        { return y; }
    public EnemyType getType()  { return type; }
    /** 0 when just placed, 1 when the enemy is about to spawn. */
    public double getProgress() { return 1.0 - timer / duration; }
}

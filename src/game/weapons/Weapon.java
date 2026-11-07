package game.weapons;

/**
 * One weapon the player is holding. Several Weapons can share the same WeaponDef
 * (two pistols), but each has its own cooldown and animation.
 */
public class Weapon {
    public static final double THRUST_TIME = 0.18; // seconds a melee stab is shown

    private final WeaponDef def;
    private double cooldownLeft;
    private double aimAngle = -Math.PI / 2;
    private double thrustTimer;
    private double thrustLength;

    public Weapon(WeaponDef def) {
        this.def = def;
    }

    public WeaponDef getDef()          { return def; }
    public boolean isReady()           { return cooldownLeft <= 0; }
    public double getAimAngle()        { return aimAngle; }
    public void setAimAngle(double a)  { aimAngle = a; }
    public void setCooldown(double s)  { cooldownLeft = s; }

    public void tick(double dt) {
        cooldownLeft -= dt;
        if (thrustTimer > 0) thrustTimer -= dt;
    }

    /** Called at the start of each wave. */
    public void reset() {
        cooldownLeft = 0.3;
        thrustTimer = 0;
    }

    public void startThrust(double length) {
        thrustLength = length;
        thrustTimer = THRUST_TIME;
    }

    /** 0 when idle, rising to 1 at full extension and back to 0. */
    public double getThrustProgress() {
        if (thrustTimer <= 0) return 0;
        double t = 1.0 - thrustTimer / THRUST_TIME;
        return Math.sin(Math.PI * t);
    }

    public double getThrustLength() {
        return thrustLength;
    }
}

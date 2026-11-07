package game.world;

import game.GameSession;
import game.items.Item;
import game.items.Player;
import game.items.Stat;
import game.weapons.Weapon;
import game.weapons.WeaponDef;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Everything that happens during one wave: movement, spawning, weapons,
 * bullets, enemy contact, pickups. Create a new World for every wave.
 *
 * This class has no drawing code. WorldRenderer (in game.ui) draws it.
 */
public class World {
    public static final double WIDTH = 1280;
    public static final double HEIGHT = 720;
    public static final double MARGIN = 24;          // thickness of the arena wall
    public static final double WEAPON_ORBIT = 30;    // weapon distance from player

    private static final double SPAWN_WARNING = 0.9; // seconds the red X is shown
    private static final double MIN_SPAWN_DIST = 220;
    private static final int MAX_ENEMIES = 160;
    private static final int MAX_TEXTS = 120;

    private static final Color TEXT_HIT = Color.WHITE;
    private static final Color TEXT_CRIT = new Color(255, 215, 60);
    private static final Color TEXT_HURT = new Color(255, 80, 80);
    private static final Color TEXT_HEAL = new Color(110, 230, 110);
    private static final Color TEXT_DODGE = new Color(140, 200, 255);

    private final GameSession session;
    private final Player player;
    private final Random rng;
    private final int wave;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Drop> drops = new ArrayList<>();
    private final List<FloatingText> texts = new ArrayList<>();
    private final List<SpawnMarker> markers = new ArrayList<>();

    private final double waveDuration;
    private double timeLeft;
    private double elapsed;
    private double spawnTimer = 0.6;
    private boolean waveOver;
    private int harvestedThisWave;

    public World(GameSession session) {
        this.session = session;
        this.player = session.getPlayer();
        this.rng = session.getRandom();
        this.wave = session.getWave();
        this.waveDuration = WaveConfig.duration(wave);
        this.timeLeft = waveDuration;

        player.setPosition(WIDTH / 2, HEIGHT / 2);
        int before = player.getMaterials();
        player.startWave();
        int bonus = player.getMaterials() - before;
        if (bonus > 0) {
            addText("+" + bonus + " materials", player.getX(), player.getY() - 50, TEXT_HEAL, true);
        }
        if (session.isFinalWave()) {
            markers.add(new SpawnMarker(EnemyType.BOSS, WIDTH / 2, 140, 2.5));
        }
    }

    // =====================================================================
    // Main update
    // =====================================================================

    /**
     * @param moveX -1 (left) to 1 (right)
     * @param moveY -1 (up) to 1 (down)
     */
    public void update(double dt, double moveX, double moveY) {
        if (waveOver || player.isDead()) return;
        elapsed += dt;
        timeLeft -= dt;

        movePlayer(dt, moveX, moveY);
        player.update(dt);
        updateSpawning(dt);
        updateEnemies(dt);
        updateWeapons(dt);
        updateProjectiles(dt);
        updateDrops(dt);
        updateTexts(dt);

        enemies.removeIf(e -> e.dead);
        projectiles.removeIf(p -> p.dead);
        drops.removeIf(d -> d.collected);

        if (!player.isDead() && timeLeft <= 0) finishWave();
    }

    private void movePlayer(double dt, double mx, double my) {
        double len = Math.sqrt(mx * mx + my * my);
        if (len > 0) {
            double speed = player.getMoveSpeed();
            double nx = player.getX() + mx / len * speed * dt;
            double ny = player.getY() + my / len * speed * dt;
            double r = Player.RADIUS;
            nx = clamp(nx, MARGIN + r, WIDTH - MARGIN - r);
            ny = clamp(ny, MARGIN + r, HEIGHT - MARGIN - r);
            player.setPosition(nx, ny);
        }
    }

    // =====================================================================
    // Spawning
    // =====================================================================

    private void updateSpawning(double dt) {
        spawnTimer -= dt;
        if (spawnTimer <= 0 && timeLeft > 2) {
            spawnTimer = WaveConfig.spawnInterval(wave);
            spawnGroup();
        }
        for (int i = markers.size() - 1; i >= 0; i--) {
            SpawnMarker m = markers.get(i);
            m.timer -= dt;
            if (m.timer <= 0) {
                enemies.add(new Enemy(m.type, m.x, m.y, wave));
                markers.remove(i);
            }
        }
    }

    private void spawnGroup() {
        if (enemies.size() + markers.size() >= MAX_ENEMIES) return;
        EnemyType type = WaveConfig.pickType(wave, rng);
        int count = WaveConfig.groupSize(wave, type, rng);
        double[] center = randomSpawnPoint();
        for (int i = 0; i < count; i++) {
            double r = type.getRadius();
            double x = clamp(center[0] + (rng.nextDouble() - 0.5) * 120, MARGIN + r, WIDTH - MARGIN - r);
            double y = clamp(center[1] + (rng.nextDouble() - 0.5) * 120, MARGIN + r, HEIGHT - MARGIN - r);
            markers.add(new SpawnMarker(type, x, y, SPAWN_WARNING));
        }
    }

    /** A random point in the arena that isn't right on top of the player. */
    private double[] randomSpawnPoint() {
        double x = 0, y = 0;
        for (int tries = 0; tries < 15; tries++) {
            x = MARGIN + 40 + rng.nextDouble() * (WIDTH - 2 * MARGIN - 80);
            y = MARGIN + 40 + rng.nextDouble() * (HEIGHT - 2 * MARGIN - 80);
            if (dist(x, y, player.getX(), player.getY()) > MIN_SPAWN_DIST) break;
        }
        return new double[] { x, y };
    }

    // =====================================================================
    // Enemies
    // =====================================================================

    private void updateEnemies(double dt) {
        double px = player.getX(), py = player.getY();
        for (Enemy e : enemies) {
            double dx = px - e.x, dy = py - e.y;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d > 1) {
                e.x += dx / d * e.type.getSpeed() * dt;
                e.y += dy / d * e.type.getSpeed() * dt;
            }
            // Knockback slides the enemy and fades out quickly.
            e.x += e.knockX * dt;
            e.y += e.knockY * dt;
            e.knockX *= Math.pow(0.001, dt);
            e.knockY *= Math.pow(0.001, dt);
            if (e.hitFlash > 0) e.hitFlash -= dt;

            double r = e.getRadius();
            e.x = clamp(e.x, MARGIN + r, WIDTH - MARGIN - r);
            e.y = clamp(e.y, MARGIN + r, HEIGHT - MARGIN - r);
        }

        separateEnemies();

        // Contact damage
        for (Enemy e : enemies) {
            if (player.isInvulnerable()) break;
            if (dist(e.x, e.y, px, py) < e.getRadius() + Player.RADIUS) {
                hurtPlayer(e.damage);
            }
        }
    }

    /** Pushes overlapping enemies apart so they don't stack into one blob. */
    private void separateEnemies() {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy a = enemies.get(i);
            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy b = enemies.get(j);
                double dx = b.x - a.x, dy = b.y - a.y;
                double minDist = a.getRadius() + b.getRadius();
                double d2 = dx * dx + dy * dy;
                if (d2 >= minDist * minDist || d2 == 0) continue;
                double d = Math.sqrt(d2);
                double overlap = (minDist - d) * 0.5;
                double nx = dx / d, ny = dy / d;
                // Big enemies get pushed less than small ones.
                double total = a.getRadius() + b.getRadius();
                double aShare = b.getRadius() / total;
                double bShare = a.getRadius() / total;
                a.x -= nx * overlap * aShare * 2;
                a.y -= ny * overlap * aShare * 2;
                b.x += nx * overlap * bShare * 2;
                b.y += ny * overlap * bShare * 2;
            }
        }
    }

    private void hurtPlayer(int damage) {
        double hpBefore = player.getCurrentHp();
        double taken = player.takeHit(damage);
        player.setInvulnerable(Player.HIT_INVULNERABILITY);
        double px = player.getX(), py = player.getY() - 30;
        if (taken == 0) {
            addText("DODGE", px, py, TEXT_DODGE, false);
            double healed = player.getCurrentHp() - hpBefore;
            if (healed > 0) addText("+" + (int) healed, px + 20, py - 15, TEXT_HEAL, false);
        } else {
            addText("-" + formatNumber(taken), px, py, TEXT_HURT, true);
        }
    }

    // =====================================================================
    // Weapons
    // =====================================================================

    /** Where weapon number i (of n) floats around the player. */
    public static double[] weaponSlotPosition(double px, double py, int i, int n) {
        double angle = -Math.PI / 2 + i * 2 * Math.PI / Math.max(1, n);
        return new double[] { px + Math.cos(angle) * WEAPON_ORBIT, py + Math.sin(angle) * WEAPON_ORBIT };
    }

    private void updateWeapons(double dt) {
        List<Weapon> weapons = player.getWeapons();
        for (int i = 0; i < weapons.size(); i++) {
            Weapon w = weapons.get(i);
            w.tick(dt);
            WeaponDef def = w.getDef();
            double[] slot = weaponSlotPosition(player.getX(), player.getY(), i, weapons.size());
            double range = player.getAttackRange(def.getRange());
            Enemy target = nearestEnemy(slot[0], slot[1], range);
            if (target == null) continue;

            double angle = Math.atan2(target.y - slot[1], target.x - slot[0]);
            w.setAimAngle(angle);
            if (!w.isReady()) continue;

            if (def.isMelee()) {
                meleeAttack(w, slot, target, range);
            } else {
                rangedAttack(w, slot, angle, range);
            }
            w.setCooldown(player.getAttackCooldown(def.getCooldown()));
        }
    }

    private void rangedAttack(Weapon w, double[] slot, double angle, double range) {
        WeaponDef def = w.getDef();
        int n = def.getProjectiles();
        for (int k = 0; k < n; k++) {
            double a;
            if (n == 1) {
                a = angle + (rng.nextDouble() - 0.5) * def.getSpread();
            } else {
                a = angle - def.getSpread() / 2 + k * def.getSpread() / (n - 1);
            }
            boolean crit = player.rollCrit();
            int dmg = player.calculateDamage(def.getDamage(), false, crit);
            projectiles.add(new Projectile(slot[0], slot[1], a, def.getProjectileSpeed(),
                    dmg, crit, def.getPierce(), range * 1.15, def.getColor()));
        }
    }

    /** A stab toward the target that hits every enemy along the line. */
    private void meleeAttack(Weapon w, double[] slot, Enemy target, double range) {
        WeaponDef def = w.getDef();
        double angle = w.getAimAngle();
        double length = Math.min(range, dist(slot[0], slot[1], target.x, target.y) + target.getRadius());
        w.startThrust(length);

        double ex = slot[0] + Math.cos(angle) * length;
        double ey = slot[1] + Math.sin(angle) * length;
        boolean crit = player.rollCrit();
        int dmg = player.calculateDamage(def.getDamage(), true, crit);
        for (Enemy e : enemies) {
            if (e.dead) continue;
            if (distToSegment(e.x, e.y, slot[0], slot[1], ex, ey) < e.getRadius() + 10) {
                damageEnemy(e, dmg, crit, angle);
            }
        }
    }

    private Enemy nearestEnemy(double x, double y, double maxRange) {
        Enemy best = null;
        double bestDist = maxRange;
        for (Enemy e : enemies) {
            if (e.dead) continue;
            double d = dist(x, y, e.x, e.y) - e.getRadius();
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    // =====================================================================
    // Projectiles
    // =====================================================================

    private void updateProjectiles(double dt) {
        for (Projectile p : projectiles) {
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.distanceLeft -= Math.sqrt(p.vx * p.vx + p.vy * p.vy) * dt;
            if (p.distanceLeft <= 0 || p.x < 0 || p.x > WIDTH || p.y < 0 || p.y > HEIGHT) {
                p.dead = true;
                continue;
            }
            for (Enemy e : enemies) {
                if (e.dead || p.alreadyHit.contains(e)) continue;
                if (dist(p.x, p.y, e.x, e.y) < e.getRadius() + Projectile.RADIUS) {
                    p.alreadyHit.add(e);
                    damageEnemy(e, p.damage, p.crit, Math.atan2(p.vy, p.vx));
                    p.pierceLeft--;
                    if (p.pierceLeft < 0) {
                        p.dead = true;
                        break;
                    }
                }
            }
        }
    }

    private void damageEnemy(Enemy e, int dmg, boolean crit, double hitAngle) {
        e.hp -= dmg;
        e.hitFlash = 0.08;
        if (e.type != EnemyType.BOSS) {
            e.knockX += Math.cos(hitAngle) * 180;
            e.knockY += Math.sin(hitAngle) * 180;
        }
        addText(crit ? dmg + "!" : String.valueOf(dmg), e.x, e.y - e.getRadius(),
                crit ? TEXT_CRIT : TEXT_HIT, crit);

        if (player.onHitEnemy()) {
            addText("+1", player.getX(), player.getY() - 35, TEXT_HEAL, false);
        }
        if (e.hp <= 0 && !e.dead) killEnemy(e);
    }

    private void killEnemy(Enemy e) {
        e.dead = true;
        session.addKill();
        for (int i = 0; i < e.type.getMaterials(); i++) {
            double jitter = Math.min(40, 6 + e.getRadius());
            drops.add(new Drop(e.x + (rng.nextDouble() - 0.5) * jitter,
                               e.y + (rng.nextDouble() - 0.5) * jitter, 1, false));
        }
        if (rng.nextDouble() < WaveConfig.crateChance(player.getStat(Stat.LUCK))) {
            drops.add(new Drop(e.x, e.y, 0, true));
        }
        if (player.addXp(1) > 0) {
            addText("LEVEL UP!  +1 Max HP", player.getX(), player.getY() - 60, TEXT_CRIT, true);
        }
    }

    // =====================================================================
    // Pickups
    // =====================================================================

    private void updateDrops(double dt) {
        double px = player.getX(), py = player.getY();
        for (Drop d : drops) {
            double dist = dist(d.x, d.y, px, py);
            if (dist < Player.RADIUS + 6) {
                collect(d);
            } else if (dist < Player.PICKUP_RANGE) {
                d.speed = Math.min(900, d.speed + 1800 * dt);
                d.x += (px - d.x) / dist * d.speed * dt;
                d.y += (py - d.y) / dist * d.speed * dt;
            }
        }
    }

    private void collect(Drop d) {
        if (d.collected) return;
        d.collected = true;
        if (d.crate) {
            Item item = session.getShop().rollItem(player, wave);
            if (item != null && player.addItem(item)) {
                addText("+ " + item.getName(), player.getX(), player.getY() - 45,
                        TEXT_CRIT, true);
            } else {
                player.addMaterials(10);
                addText("+10", player.getX(), player.getY() - 45, TEXT_HEAL, true);
            }
        } else {
            player.addMaterials(d.value);
            session.addMaterialsCollected(d.value);
        }
    }

    // =====================================================================
    // Wave end
    // =====================================================================

    private void finishWave() {
        // Anything still on the floor is collected automatically.
        for (Drop d : drops) collect(d);
        drops.clear();
        enemies.clear();
        markers.clear();
        projectiles.clear();
        harvestedThisWave = player.endWave();
        waveOver = true;
    }

    // =====================================================================
    // Floating text
    // =====================================================================

    private void addText(String text, double x, double y, Color color, boolean big) {
        if (texts.size() >= MAX_TEXTS) texts.remove(0);
        double jitter = (rng.nextDouble() - 0.5) * 16;
        texts.add(new FloatingText(text, x + jitter, y, color, big));
    }

    private void updateTexts(double dt) {
        for (int i = texts.size() - 1; i >= 0; i--) {
            FloatingText t = texts.get(i);
            t.life -= dt;
            t.y -= 45 * dt;
            if (t.life <= 0) texts.remove(i);
        }
    }

    // =====================================================================
    // Getters for the renderer / HUD
    // =====================================================================

    public Player getPlayer()               { return player; }
    public int getWave()                    { return wave; }
    public double getTimeLeft()             { return Math.max(0, timeLeft); }
    public double getElapsed()              { return elapsed; }
    public boolean isWaveOver()             { return waveOver; }
    public int getHarvestedThisWave()       { return harvestedThisWave; }
    public List<Enemy> getEnemies()         { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Drop> getDrops()            { return drops; }
    public List<FloatingText> getTexts()    { return texts; }
    public List<SpawnMarker> getMarkers()   { return markers; }

    // =====================================================================
    // Math helpers
    // =====================================================================

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double dist(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double distToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double len2 = dx * dx + dy * dy;
        double t = len2 == 0 ? 0 : ((px - x1) * dx + (py - y1) * dy) / len2;
        t = clamp(t, 0, 1);
        return dist(px, py, x1 + t * dx, y1 + t * dy);
    }

    private static String formatNumber(double v) {
        return Math.abs(v - Math.round(v)) < 0.05 ? String.valueOf(Math.round(v))
                                                  : String.format("%.1f", v);
    }
}

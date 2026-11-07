package game.items;

import game.weapons.Weapon;
import game.weapons.WeaponDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The player: stats, items, weapons, materials, HP, level and position.
 *
 * Key idea: final stats are never edited directly. They are always rebuilt as
 * base stats + every owned item's modifiers (see recalculateStats). That makes
 * selling/removing items, save files and debugging simple.
 */
public class Player {

    public enum PurchaseResult { SUCCESS, NOT_ENOUGH_MATERIALS, MAX_OWNED, WEAPONS_FULL }

    public static final int MAX_WEAPONS = 6;
    public static final int START_MAX_HP = 15;           // Brotato uses 10, but also has level-up upgrades
    public static final double RADIUS = 18;            // collision size in pixels
    public static final double BASE_MOVE_SPEED = 230;  // pixels per second at 0% Speed
    public static final double PICKUP_RANGE = 90;      // materials fly to you inside this
    public static final double HIT_INVULNERABILITY = 0.5; // seconds of safety after a hit

    private final PlayerStats baseStats;
    private PlayerStats stats;
    private final List<Item> inventory = new ArrayList<>();
    private final List<Weapon> weapons = new ArrayList<>();
    private final Random rng;

    private int materials;
    private double currentHp;
    private double regenTimer;
    private double invulnTimer;
    private double x, y;
    private int level = 1;
    private int xp;

    public Player(Random rng) {
        this.rng = rng;
        baseStats = new PlayerStats();
        baseStats.set(Stat.MAX_HP, START_MAX_HP);
        stats = baseStats.copy();
        currentHp = getMaxHp();
    }

    public Player() {
        this(new Random());
    }

    // =====================================================================
    // Items
    // =====================================================================

    public int countOf(Item item) {
        int n = 0;
        for (Item owned : inventory) {
            if (owned == item) n++;
        }
        return n;
    }

    /** True if the player is allowed to own another copy (ignores price). */
    public boolean canOwnMore(Item item) {
        return !item.isLimited() || countOf(item) < item.getMaxCount();
    }

    public PurchaseResult checkPurchase(Item item) {
        if (!canOwnMore(item)) return PurchaseResult.MAX_OWNED;
        if (materials < item.getPrice()) return PurchaseResult.NOT_ENOUGH_MATERIALS;
        return PurchaseResult.SUCCESS;
    }

    /** Pays for the item and adds it. The UI can use the result to show a message. */
    public PurchaseResult buy(Item item) {
        PurchaseResult result = checkPurchase(item);
        if (result == PurchaseResult.SUCCESS) {
            materials -= item.getPrice();
            addItem(item);
        }
        return result;
    }

    /** Adds an item for free (crate drop, starting item...). Returns false if at max. */
    public boolean addItem(Item item) {
        if (!canOwnMore(item)) return false;
        double oldMax = getMaxHp();
        inventory.add(item);
        recalculateStats();
        // Gaining Max HP also heals by that amount, like in Brotato.
        double gained = getMaxHp() - oldMax;
        if (gained > 0) currentHp += gained;
        return true;
    }

    public boolean removeItem(Item item) {
        boolean removed = inventory.remove(item);
        if (removed) recalculateStats();
        return removed;
    }

    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }

    /** Each different item the player owns, once, in the order first bought. */
    public List<Item> getUniqueItems() {
        List<Item> unique = new ArrayList<>();
        for (Item item : inventory) {
            if (!unique.contains(item)) unique.add(item);
        }
        return unique;
    }

    private void recalculateStats() {
        PlayerStats s = baseStats.copy();
        for (Item item : inventory) {
            s.addAll(item.getModifiers());
        }
        stats = s;
        currentHp = Math.min(currentHp, getMaxHp());
    }

    // =====================================================================
    // Weapons
    // =====================================================================

    public List<Weapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    public boolean addWeapon(WeaponDef def) {
        if (weapons.size() >= MAX_WEAPONS) return false;
        weapons.add(new Weapon(def));
        return true;
    }

    public PurchaseResult checkPurchase(WeaponDef def) {
        if (weapons.size() >= MAX_WEAPONS) return PurchaseResult.WEAPONS_FULL;
        if (materials < def.getPrice()) return PurchaseResult.NOT_ENOUGH_MATERIALS;
        return PurchaseResult.SUCCESS;
    }

    public PurchaseResult buy(WeaponDef def) {
        PurchaseResult result = checkPurchase(def);
        if (result == PurchaseResult.SUCCESS) {
            materials -= def.getPrice();
            addWeapon(def);
        }
        return result;
    }

    /** Sells the weapon in that slot for half price. You must keep at least one. */
    public boolean sellWeapon(int index) {
        if (weapons.size() <= 1 || index < 0 || index >= weapons.size()) return false;
        Weapon w = weapons.remove(index);
        materials += w.getDef().getSellPrice();
        return true;
    }

    // =====================================================================
    // Leveling: +1 Max HP per level, like Brotato (without the upgrade picks)
    // =====================================================================

    public int getLevel() { return level; }
    public int getXp()    { return xp; }

    public int getXpToNextLevel() {
        return (level + 3) * (level + 3);   // Brotato's formula
    }

    /** Adds XP and returns how many levels were gained (usually 0 or 1). */
    public int addXp(int amount) {
        xp += amount;
        int gained = 0;
        while (xp >= getXpToNextLevel()) {
            xp -= getXpToNextLevel();
            level++;
            gained++;
            baseStats.add(Stat.MAX_HP, 1);
            recalculateStats();
            heal(1);
        }
        return gained;
    }

    // =====================================================================
    // Wave lifecycle - called by World
    // =====================================================================

    /** Call when a wave begins (after leaving the shop). */
    public void startWave() {
        currentHp = getMaxHp();   // Brotato fully heals you between waves
        invulnTimer = 0;
        regenTimer = 0;
        for (Weapon w : weapons) w.reset();
        for (Item item : inventory) {
            if (item.hasEffect()) item.getEffect().onWaveStart(this);
        }
    }

    /** Call when a wave ends, before opening the shop. Returns materials harvested. */
    public int endWave() {
        int harvest = stats.get(Stat.HARVESTING);
        if (harvest > 0) addMaterials(harvest);
        return Math.max(0, harvest);
    }

    /** Call every frame with the frame time in seconds. */
    public void update(double deltaSeconds) {
        if (invulnTimer > 0) invulnTimer -= deltaSeconds;

        int regen = stats.get(Stat.HP_REGEN);
        if (regen <= 0 || currentHp >= getMaxHp()) {
            regenTimer = 0;
            return;
        }
        // Brotato's formula: heal 1 HP every 5 / (1 + (regen - 1) / 2.25) seconds.
        double interval = 5.0 / (1.0 + (regen - 1) / 2.25);
        regenTimer += deltaSeconds;
        while (regenTimer >= interval) {
            regenTimer -= interval;
            heal(1);
        }
    }

    // =====================================================================
    // Combat
    // =====================================================================

    /**
     * Call when an enemy hits the player. Handles dodge, armor and on-dodge effects.
     * @return damage actually taken (0 if dodged)
     */
    public double takeHit(double rawDamage) {
        if (rng.nextDouble() * 100 < stats.get(Stat.DODGE)) {
            for (Item item : inventory) {
                if (item.hasEffect()) item.getEffect().onDodge(this, rng);
            }
            return 0;
        }
        double dmg = Math.max(1, rawDamage * getDamageTakenMultiplier());
        currentHp = Math.max(0, currentHp - dmg);
        return dmg;
    }

    public boolean isInvulnerable()            { return invulnTimer > 0; }
    public void setInvulnerable(double secs)   { invulnTimer = secs; }

    /**
     * Positive armor reduces damage by armor / (armor + 15): 5 armor = 25% less.
     * Negative armor increases damage taken (simplified version of Brotato).
     */
    public double getDamageTakenMultiplier() {
        int armor = stats.get(Stat.ARMOR);
        if (armor >= 0) return 1.0 - armor / (armor + 15.0);
        return 1.0 + (-armor) / 15.0;
    }

    /** Damage for one weapon hit, rolling for a crit. */
    public int calculateDamage(int weaponBaseDamage, boolean isMelee) {
        return calculateDamage(weaponBaseDamage, isMelee, rollCrit());
    }

    /**
     * Adds the flat Melee or Ranged bonus, then applies %Damage. A crit doubles it.
     * Returns at least 1.
     */
    public int calculateDamage(int weaponBaseDamage, boolean isMelee, boolean crit) {
        int flat = isMelee ? stats.get(Stat.MELEE_DAMAGE) : stats.get(Stat.RANGED_DAMAGE);
        double dmg = (weaponBaseDamage + flat) * (1.0 + stats.get(Stat.DAMAGE) / 100.0);
        if (crit) dmg *= 2;
        return Math.max(1, (int) Math.round(dmg));
    }

    public boolean rollCrit() {
        return rng.nextDouble() * 100 < stats.get(Stat.CRIT_CHANCE);
    }

    /**
     * Call whenever one of the player's attacks hits an enemy. Handles life steal.
     * @return true if the player healed
     */
    public boolean onHitEnemy() {
        if (currentHp < getMaxHp() && rng.nextDouble() * 100 < stats.get(Stat.LIFE_STEAL)) {
            heal(1);
            return true;
        }
        return false;
    }

    /** Seconds between attacks for a weapon. +100% attack speed halves it. */
    public double getAttackCooldown(double weaponBaseCooldown) {
        double mult = 1.0 + stats.get(Stat.ATTACK_SPEED) / 100.0;
        return weaponBaseCooldown / Math.max(0.1, mult);
    }

    /** Weapon range plus the Range stat (never below 40 so weapons still work). */
    public double getAttackRange(double weaponBaseRange) {
        return Math.max(40, weaponBaseRange + stats.get(Stat.RANGE));
    }

    /** Movement speed in pixels per second. */
    public double getMoveSpeed() {
        return BASE_MOVE_SPEED * Math.max(0.1, 1.0 + stats.get(Stat.SPEED) / 100.0);
    }

    // =====================================================================
    // Basic getters / setters
    // =====================================================================

    public void heal(double amount) {
        currentHp = Math.min(getMaxHp(), currentHp + amount);
    }

    public boolean isDead()              { return currentHp <= 0; }
    public double getCurrentHp()         { return currentHp; }
    public int getMaxHp()                { return stats.get(Stat.MAX_HP); }
    public int getStat(Stat stat)        { return stats.get(stat); }
    public PlayerStats getStats()        { return stats; }
    public int getMaterials()            { return materials; }
    public void addMaterials(int amount) { materials += amount; }
    public boolean spendMaterials(int amount) {
        if (materials < amount) return false;
        materials -= amount;
        return true;
    }
    public Random getRandom()            { return rng; }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

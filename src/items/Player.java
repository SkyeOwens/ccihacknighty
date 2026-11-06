package src.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Holds the player's inventory and materials, and turns stats into gameplay
 * numbers.
 *
 * Key idea: final stats are never edited directly. They are always rebuilt as
 * base stats + every owned item's modifiers (see recalculateStats). That makes
 * selling/removing items, save files and debugging simple.
 */
public class Player {

    public enum PurchaseResult {
        SUCCESS, NOT_ENOUGH_MATERIALS, MAX_OWNED
    }

    private final PlayerStats baseStats;
    private PlayerStats stats;
    private final List<Item> inventory = new ArrayList<>();
    private final Random rng;

    private int materials;
    private double currentHp;
    private double regenTimer;

    public Player(Random rng) {
        this.rng = rng;
        baseStats = new PlayerStats();
        baseStats.set(Stat.MAX_HP, 10); // Brotato starts you at 10 Max HP
        stats = baseStats.copy();
        currentHp = getMaxHp();
    }

    public Player() {
        this(new Random());
    }

    // =====================================================================
    // Inventory & shop
    // =====================================================================

    public int countOf(Item item) {
        int n = 0;
        for (Item owned : inventory) {
            if (owned == item)
                n++;
        }
        return n;
    }

    /** True if the player is allowed to own another copy (ignores price). */
    public boolean canOwnMore(Item item) {
        return !item.isLimited() || countOf(item) < item.getMaxCount();
    }

    public PurchaseResult checkPurchase(Item item) {
        if (!canOwnMore(item))
            return PurchaseResult.MAX_OWNED;
        if (materials < item.getPrice())
            return PurchaseResult.NOT_ENOUGH_MATERIALS;
        return PurchaseResult.SUCCESS;
    }

    /**
     * Pays for the item and adds it. The UI can use the result to show a message.
     */
    public PurchaseResult buy(Item item) {
        PurchaseResult result = checkPurchase(item);
        if (result == PurchaseResult.SUCCESS) {
            materials -= item.getPrice();
            addItem(item);
        }
        return result;
    }

    /**
     * Adds an item for free (crate drop, starting item...). Returns false if at
     * max.
     */
    public boolean addItem(Item item) {
        if (!canOwnMore(item))
            return false;
        double oldMax = getMaxHp();
        inventory.add(item);
        recalculateStats();
        // Gaining Max HP also heals by that amount, like in Brotato.
        double gained = getMaxHp() - oldMax;
        if (gained > 0)
            currentHp += gained;
        return true;
    }

    public boolean removeItem(Item item) {
        boolean removed = inventory.remove(item);
        if (removed)
            recalculateStats();
        return removed;
    }

    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
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
    // Wave lifecycle - call these from your game loop
    // =====================================================================

    /** Call when a wave begins (after leaving the shop). */
    public void startWave() {
        for (Item item : inventory) {
            if (item.hasEffect())
                item.getEffect().onWaveStart(this);
        }
    }

    /** Call when a wave ends, before opening the shop. */
    public void endWave() {
        // Harvesting gives bonus materials at the end of each wave.
        int harvest = stats.get(Stat.HARVESTING);
        if (harvest > 0)
            addMaterials(harvest);
    }

    /** Call every frame with the frame time in seconds. Handles HP regen. */
    public void update(double deltaSeconds) {
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
    // Combat - your weapon / enemy code calls these
    // =====================================================================

    /**
     * Call when an enemy hits the player. Handles dodge, armor and on-dodge
     * effects.
     * 
     * @return damage actually taken (0 if dodged)
     */
    public double takeHit(double rawDamage) {
        if (rng.nextDouble() * 100 < stats.get(Stat.DODGE)) {
            for (Item item : inventory) {
                if (item.hasEffect())
                    item.getEffect().onDodge(this, rng);
            }
            return 0;
        }
        double dmg = Math.max(1, rawDamage * getDamageTakenMultiplier());
        currentHp = Math.max(0, currentHp - dmg);
        return dmg;
    }

    /**
     * Positive armor reduces damage by armor / (armor + 15): 5 armor = 25% less.
     * Negative armor increases damage taken (simplified version of Brotato).
     */
    public double getDamageTakenMultiplier() {
        int armor = stats.get(Stat.ARMOR);
        if (armor >= 0)
            return 1.0 - armor / (armor + 15.0);
        return 1.0 + (-armor) / 15.0;
    }

    /**
     * Damage for one weapon hit. Adds the flat Melee or Ranged bonus, then applies
     * %Damage.
     * Crit doubles the hit. Returns at least 1.
     */
    public int calculateDamage(int weaponBaseDamage, boolean isMelee) {
        int flat = isMelee ? stats.get(Stat.MELEE_DAMAGE) : stats.get(Stat.RANGED_DAMAGE);
        double dmg = (weaponBaseDamage + flat) * (1.0 + stats.get(Stat.DAMAGE) / 100.0);
        if (rollCrit())
            dmg *= 2;
        return Math.max(1, (int) Math.round(dmg));
    }

    public boolean rollCrit() {
        return rng.nextDouble() * 100 < stats.get(Stat.CRIT_CHANCE);
    }

    /**
     * Call whenever one of the player's attacks hits an enemy. Handles life steal.
     */
    public void onHitEnemy() {
        if (rng.nextDouble() * 100 < stats.get(Stat.LIFE_STEAL)) {
            heal(1);
        }
    }

    /** Seconds between attacks for a weapon. +100% attack speed halves it. */
    public double getAttackCooldown(double weaponBaseCooldown) {
        double mult = 1.0 + stats.get(Stat.ATTACK_SPEED) / 100.0;
        return weaponBaseCooldown / Math.max(0.1, mult);
    }

    /** Weapon range plus the Range stat (never below 10 so weapons still work). */
    public double getAttackRange(double weaponBaseRange) {
        return Math.max(10, weaponBaseRange + stats.get(Stat.RANGE));
    }

    /** Movement speed in pixels/sec (or whatever unit your base speed uses). */
    public double getMoveSpeed(double baseMoveSpeed) {
        return baseMoveSpeed * Math.max(0.1, 1.0 + stats.get(Stat.SPEED) / 100.0);
    }

    // =====================================================================
    // Basic getters / setters
    // =====================================================================

    public void heal(double amount) {
        currentHp = Math.min(getMaxHp(), currentHp + amount);
    }

    public boolean isDead() {
        return currentHp <= 0;
    }

    public double getCurrentHp() {
        return currentHp;
    }

    public int getMaxHp() {
        return stats.get(Stat.MAX_HP);
    }

    public int getStat(Stat stat) {
        return stats.get(stat);
    }

    public PlayerStats getStats() {
        return stats;
    }

    public int getMaterials() {
        return materials;
    }

    public void addMaterials(int amount) {
        materials += amount;
    }

    public Random getRandom() {
        return rng;
    }
}

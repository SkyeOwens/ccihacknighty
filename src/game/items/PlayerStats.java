package game.items;

import java.util.EnumMap;
import java.util.Map;

/**
 * A bag of stat values. Used both for the player's base stats and for the final
 * stats after all items are applied.
 */
public class PlayerStats {
    // Brotato-style caps. Change these to tune your game.
    public static final int MAX_DODGE = 60;
    public static final int MAX_CRIT = 100;
    public static final int MAX_LIFE_STEAL = 100;
    public static final int MIN_MAX_HP = 1;

    private final EnumMap<Stat, Integer> values = new EnumMap<>(Stat.class);

    public PlayerStats() {
        for (Stat s : Stat.values()) {
            values.put(s, 0);
        }
    }

    /** Raw value, no caps applied (may be negative or above caps). */
    public int getRaw(Stat stat) {
        return values.get(stat);
    }

    /** Value with caps applied. This is what gameplay code should read. */
    public int get(Stat stat) {
        int v = values.get(stat);
        switch (stat) {
            case DODGE:      return Math.min(v, MAX_DODGE);
            case CRIT_CHANCE: return clamp(v, 0, MAX_CRIT);
            case LIFE_STEAL: return clamp(v, 0, MAX_LIFE_STEAL);
            case MAX_HP:     return Math.max(v, MIN_MAX_HP);
            default:         return v;
        }
    }

    public void set(Stat stat, int value) {
        values.put(stat, value);
    }

    public void add(Stat stat, int amount) {
        values.put(stat, values.get(stat) + amount);
    }

    public void addAll(Map<Stat, Integer> modifiers) {
        for (Map.Entry<Stat, Integer> e : modifiers.entrySet()) {
            add(e.getKey(), e.getValue());
        }
    }

    public PlayerStats copy() {
        PlayerStats c = new PlayerStats();
        c.values.putAll(this.values);
        return c;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Stat s : Stat.values()) {
            sb.append(String.format("  %-13s %4d%s%n",
                    s.getDisplayName(), get(s), s.isPercent() ? "%" : ""));
        }
        return sb.toString();
    }
}

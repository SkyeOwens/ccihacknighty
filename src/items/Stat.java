package src.items;

/**
 * Every stat an item can change.
 *
 * Percentage stats (Damage, Attack Speed, Crit, Dodge, Speed, Life Steal) are
 * stored
 * as whole numbers: a value of 12 means 12%.
 */
public enum Stat {
    MAX_HP("Max HP", false),
    HP_REGEN("HP Regen", false),
    LIFE_STEAL("Life Steal", true),
    DAMAGE("Damage", true),
    MELEE_DAMAGE("Melee", false),
    RANGED_DAMAGE("Ranged", false),
    ATTACK_SPEED("Attack Speed", true),
    CRIT_CHANCE("Crit", true),
    RANGE("Range", false),
    ARMOR("Armor", false),
    DODGE("Dodge", true),
    SPEED("Speed", true),
    LUCK("Luck", false),
    HARVESTING("Harvesting", false);

    private final String displayName;
    private final boolean percent;

    Stat(String displayName, boolean percent) {
        this.displayName = displayName;
        this.percent = percent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPercent() {
        return percent;
    }

    /** Formats a modifier for the UI, e.g. "+12% Damage" or "-5 Range". */
    public String format(int value) {
        String sign = value >= 0 ? "+" : "";
        return sign + value + (percent ? "% " : " ") + displayName;
    }
}

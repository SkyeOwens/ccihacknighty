package game.items;

import game.weapons.WeaponDef;
import game.weapons.Weapons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rolls the cards offered between waves. Each slot is either a weapon or an item.
 * Higher waves and higher Luck make higher item tiers more likely. The odds are
 * our own simple curve, not Brotato's exact table; tweak them to balance the game.
 */
public class Shop {
    public static final int SLOTS = 4;
    public static final double WEAPON_CHANCE = 0.35;

    private final Random rng;

    public Shop(Random rng) {
        this.rng = rng;
    }

    /** Chance (0 to 1) of each tier for a shop slot. Index 0 = Tier 1. */
    public double[] getTierChances(int wave, int luck) {
        double luckMult = Math.max(0, 1.0 + luck / 100.0);
        double t4 = wave >= 8 ? Math.min(0.10, 0.01 * (wave - 7)) : 0;
        double t3 = wave >= 4 ? Math.min(0.25, 0.03 * (wave - 3)) : 0;
        double t2 = wave >= 2 ? Math.min(0.40, 0.08 * (wave - 1)) : 0;
        t4 *= luckMult;
        t3 *= luckMult;
        t2 *= luckMult;
        // Make sure the total never goes above 100%.
        double total = t2 + t3 + t4;
        if (total > 0.95) {
            double scale = 0.95 / total;
            t2 *= scale;
            t3 *= scale;
            t4 *= scale;
        }
        return new double[] { 1.0 - t2 - t3 - t4, t2, t3, t4 };
    }

    /** Returns SLOTS offers for the shop screen. */
    public List<ShopOffer> roll(Player player, int wave) {
        List<ShopOffer> offers = new ArrayList<>();
        List<Item> itemsShown = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            if (rng.nextDouble() < WEAPON_CHANCE) {
                offers.add(ShopOffer.of(randomWeapon()));
            } else {
                Item item = rollItem(player, wave, itemsShown);
                if (item != null) {
                    itemsShown.add(item);
                    offers.add(ShopOffer.of(item));
                } else {
                    offers.add(ShopOffer.of(randomWeapon()));
                }
            }
        }
        return offers;
    }

    public WeaponDef randomWeapon() {
        return Weapons.ALL.get(rng.nextInt(Weapons.ALL.size()));
    }

    /** A random item the player is allowed to own. Also used for loot crates. */
    public Item rollItem(Player player, int wave) {
        return rollItem(player, wave, new ArrayList<Item>());
    }

    private Item rollItem(Player player, int wave, List<Item> alreadyShown) {
        int tier = pickTier(getTierChances(wave, player.getStat(Stat.LUCK)));
        // First try to avoid showing the same item twice; if that's impossible, allow it.
        for (int attempt = 0; attempt < 2; attempt++) {
            boolean allowDuplicates = attempt == 1;
            // If the rolled tier has nothing available, fall back to lower tiers.
            for (int t = tier; t >= 1; t--) {
                List<Item> pool = new ArrayList<>();
                for (Item item : Items.ofTier(t)) {
                    boolean shown = alreadyShown.contains(item);
                    boolean blocked = shown && (item.isLimited() || !allowDuplicates);
                    if (player.canOwnMore(item) && !blocked) pool.add(item);
                }
                if (!pool.isEmpty()) return pool.get(rng.nextInt(pool.size()));
            }
        }
        return null;
    }

    private int pickTier(double[] chances) {
        double r = rng.nextDouble();
        double cumulative = 0;
        for (int i = 0; i < chances.length; i++) {
            cumulative += chances[i];
            if (r < cumulative) return i + 1;
        }
        return 1;
    }
}

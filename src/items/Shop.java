package src.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rolls the items offered between waves. Higher waves and higher Luck make
 * higher tiers more likely. The odds below are our own simple curve, not
 * Brotato's exact table; tweak them to balance your game.
 */
public class Shop {
    public static final int SLOTS = 4;

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

    /** Returns SLOTS items to show, skipping items the player can't own more of. */
    public List<Item> roll(Player player, int wave) {
        double[] chances = getTierChances(wave, player.getStat(Stat.LUCK));
        List<Item> offers = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            Item item = rollOne(player, chances, offers);
            if (item != null)
                offers.add(item);
        }
        return offers;
    }

    private Item rollOne(Player player, double[] chances, List<Item> alreadyOffered) {
        int tier = pickTier(chances);
        // If the rolled tier has nothing available, fall back to lower tiers.
        for (int t = tier; t >= 1; t--) {
            List<Item> pool = new ArrayList<>();
            for (Item item : Items.ofTier(t)) {
                boolean limitedAndShown = item.isLimited() && alreadyOffered.contains(item);
                if (player.canOwnMore(item) && !limitedAndShown)
                    pool.add(item);
            }
            if (!pool.isEmpty())
                return pool.get(rng.nextInt(pool.size()));
        }
        return null;
    }

    private int pickTier(double[] chances) {
        double r = rng.nextDouble();
        double cumulative = 0;
        for (int i = 0; i < chances.length; i++) {
            cumulative += chances[i];
            if (r < cumulative)
                return i + 1;
        }
        return 1;
    }
}

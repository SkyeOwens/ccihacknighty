package game.items;

import java.util.List;
import java.util.Random;

/** Console demo showing how the item system is used. Not needed in the real game. */
public class ItemDemo {
    public static void main(String[] args) {
        Random rng = new Random(2);
        Player player = new Player(rng);
        Shop shop = new Shop(rng);

        System.out.println("=== All items ===");
        for (int tier = 1; tier <= 4; tier++) {
            System.out.println("Tier " + tier + ":");
            for (Item item : Items.ofTier(tier)) {
                System.out.println("  " + item);
            }
        }

        player.addMaterials(500);
        System.out.println("\n=== Buying items (500 materials) ===");
        Item[] wishlist = { Items.HELMET, Items.HELMET, Items.COFFEE, Items.PIGGY_BANK,
                            Items.PIGGY_BANK, Items.ADRENALINE, Items.GLASS_CANNON,
                            Items.CAKE, Items.CAPE };
        for (Item item : wishlist) {
            Player.PurchaseResult r = player.buy(item);
            System.out.printf("  %-13s -> %-20s materials left: %d%n",
                    item.getName(), r, player.getMaterials());
        }

        System.out.println("\n=== Final stats ===");
        System.out.print(player.getStats());

        System.out.println("\n=== Stats in gameplay ===");
        System.out.printf("  Damage taken multiplier: %.2f%n", player.getDamageTakenMultiplier());
        System.out.printf("  Pistol (1.2s cooldown) fires every %.2fs%n", player.getAttackCooldown(1.2));
        System.out.printf("  Move speed: %.1f px/s%n", player.getMoveSpeed());
        System.out.println("  Ranged hit with 10 base damage: " + player.calculateDamage(10, false));

        System.out.println("\n=== Piggy Bank at wave start ===");
        System.out.println("  Before: " + player.getMaterials());
        player.startWave();
        System.out.println("  After:  " + player.getMaterials());

        System.out.println("\n=== Taking hits of 2 damage (25% dodge; Adrenaline may heal on dodge) ===");
        for (int i = 0; i < 12 && !player.isDead(); i++) {
            double taken = player.takeHit(2);
            System.out.printf("  hit %2d: %s, HP now %.1f / %d%n", i + 1,
                    taken == 0 ? "DODGED" : "took " + String.format("%.1f", taken),
                    player.getCurrentHp(), player.getMaxHp());
        }

        System.out.println("\n=== Shop rolls ===");
        for (int wave : new int[] { 1, 5, 10, 15 }) {
            List<ShopOffer> offers = shop.roll(player, wave);
            StringBuilder sb = new StringBuilder();
            for (ShopOffer o : offers) {
                sb.append(o.getName()).append(o.isWeapon() ? " (weapon)  " : " (T" + o.getTier() + ")  ");
            }
            System.out.println("  Wave " + wave + ": " + sb);
        }
    }
}

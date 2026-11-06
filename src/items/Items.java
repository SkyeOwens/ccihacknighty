package src.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static src.items.Stat.*;

/**
 * Every item in the game. To add an item: define it here and add it to ALL.
 */
public final class Items {
        private Items() {
        }

        // ---------------- Tier 1 ----------------
        public static final Item HELMET = Item.builder("helmet", "Helmet", 1, 15)
                        .stat(ARMOR, 1).stat(SPEED, -2).build();

        public static final Item CAKE = Item.builder("cake", "Cake", 1, 15)
                        .stat(MAX_HP, 3).stat(DAMAGE, -1).build();

        public static final Item MUSHROOM = Item.builder("mushroom", "Mushroom", 1, 25)
                        .stat(HP_REGEN, 3).stat(LUCK, -2).build();

        public static final Item COFFEE = Item.builder("coffee", "Coffee", 1, 20)
                        .stat(ATTACK_SPEED, 10).stat(DAMAGE, -2).build();

        public static final Item GOAT_SKULL = Item.builder("goat_skull", "Goat Skull", 1, 25)
                        .stat(MELEE_DAMAGE, 3).stat(CRIT_CHANCE, -2).build();

        public static final Item LENS = Item.builder("lens", "Lens", 1, 20)
                        .stat(RANGED_DAMAGE, 1).stat(RANGE, -5).build();

        public static final Item FERTILIZER = Item.builder("fertilizer", "Fertilizer", 1, 15)
                        .stat(HARVESTING, 8).stat(MELEE_DAMAGE, -1).build();

        // ---------------- Tier 2 ----------------
        public static final Item LEATHER_VEST = Item.builder("leather_vest", "Leather Vest", 2, 45)
                        .stat(ARMOR, 2).stat(DODGE, 6).stat(MAX_HP, -3).build();

        public static final Item CYCLOPS_WORM = Item.builder("cyclops_worm", "Cyclops Worm", 2, 45)
                        .stat(DAMAGE, 12).stat(RANGE, -12).build();

        public static final Item SCOPE = Item.builder("scope", "Scope", 2, 48)
                        .stat(RANGED_DAMAGE, 2).stat(RANGE, 25).stat(ATTACK_SPEED, -7).build();

        public static final Item BLINDFOLD = Item.builder("blindfold", "Blindfold", 2, 45)
                        .stat(CRIT_CHANCE, 5).stat(DODGE, 5).stat(RANGE, -15).build();

        public static final Item BLOOD_LEECH = Item.builder("blood_leech", "Blood Leech", 2, 45)
                        .stat(LIFE_STEAL, 2).stat(HP_REGEN, 2).stat(HARVESTING, -3).build();

        public static final Item PIGGY_BANK = Item.builder("piggy_bank", "Piggy Bank", 2, 40)
                        .max(1)
                        .effect("+20% of your materials when a new wave starts", new ItemEffect() {
                                @Override
                                public void onWaveStart(Player player) {
                                        player.addMaterials(player.getMaterials() * 20 / 100);
                                }
                        })
                        .build();

        // ---------------- Tier 3 ----------------
        public static final Item GLASS_CANNON = Item.builder("glass_cannon", "Glass Cannon", 3, 75)
                        .stat(DAMAGE, 25).stat(ARMOR, -3).build();

        public static final Item LUCKY_CHARM = Item.builder("lucky_charm", "Lucky Charm", 3, 75)
                        .stat(LUCK, 30).stat(MELEE_DAMAGE, -2).stat(RANGED_DAMAGE, -1).build();

        public static final Item FIN = Item.builder("fin", "Fin", 3, 65)
                        .stat(SPEED, 10).stat(LIFE_STEAL, 3).stat(LUCK, -8).build();

        public static final Item ADRENALINE = Item.builder("adrenaline", "Adrenaline", 3, 60)
                        .max(1)
                        .stat(DODGE, 5)
                        .effect("50% chance to heal 5 HP when dodging", new ItemEffect() {
                                @Override
                                public void onDodge(Player player, Random rng) {
                                        if (rng.nextDouble() < 0.50) {
                                                player.heal(5);
                                        }
                                }
                        })
                        .build();

        // ---------------- Tier 4 ----------------
        public static final Item POTATO = Item.builder("potato", "Potato", 4, 95)
                        .stat(MAX_HP, 3).stat(HP_REGEN, 2).stat(LIFE_STEAL, 1)
                        .stat(DAMAGE, 5).stat(ATTACK_SPEED, 5).stat(SPEED, 3)
                        .stat(DODGE, 3).stat(ARMOR, 1).stat(LUCK, 5).build();

        public static final Item BIG_ARMS = Item.builder("big_arms", "Big Arms", 4, 105)
                        .stat(MELEE_DAMAGE, 12).stat(RANGED_DAMAGE, 6)
                        .stat(ATTACK_SPEED, -3).stat(SPEED, -3).build();

        public static final Item CAPE = Item.builder("cape", "Cape", 4, 110)
                        .stat(LIFE_STEAL, 5).stat(DODGE, 20)
                        .stat(MELEE_DAMAGE, -2).stat(RANGED_DAMAGE, -2).build();

        // ---------------- Lookups ----------------
        public static final List<Item> ALL = Collections.unmodifiableList(Arrays.asList(
                        HELMET, CAKE, MUSHROOM, COFFEE, GOAT_SKULL, LENS, FERTILIZER,
                        LEATHER_VEST, CYCLOPS_WORM, SCOPE, BLINDFOLD, BLOOD_LEECH, PIGGY_BANK,
                        GLASS_CANNON, LUCKY_CHARM, FIN, ADRENALINE,
                        POTATO, BIG_ARMS, CAPE));

        public static List<Item> ofTier(int tier) {
                List<Item> result = new ArrayList<>();
                for (Item item : ALL) {
                        if (item.getTier() == tier) {
                                result.add(item);
                        }
                }
                return result;
        }

        /** Find an item by id, e.g. for save files. Returns null if not found. */
        public static Item byId(String id) {
                for (Item item : ALL) {
                        if (item.getId().equals(id)) {
                                return item;
                        }
                }
                return null;
        }
}

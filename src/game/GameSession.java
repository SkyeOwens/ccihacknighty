package game;

import game.items.Player;
import game.items.Shop;
import game.items.ShopOffer;
import game.weapons.WeaponDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * One run of the game, from picking a starting weapon to winning or dying.
 * Holds everything that survives between waves.
 */
public class GameSession {
    public static final int FINAL_WAVE = 10;
    public static final int REROLL_INCREASE = 2;

    private final Random rng;
    private final Player player;
    private final Shop shop;
    private int wave = 1;

    private List<ShopOffer> offers = new ArrayList<>();
    private int rerollCost;

    private int kills;
    private int materialsCollected;

    public GameSession(WeaponDef startingWeapon) {
        this(startingWeapon, new Random());
    }

    public GameSession(WeaponDef startingWeapon, Random rng) {
        this.rng = rng;
        this.player = new Player(rng);
        this.shop = new Shop(rng);
        player.addWeapon(startingWeapon);
    }

    // ---------------- Waves ----------------

    public int getWave()          { return wave; }
    public boolean isFinalWave()  { return wave >= FINAL_WAVE; }
    public void advanceWave()     { wave++; }

    // ---------------- Shop ----------------

    /** Call when a wave ends. Rolls fresh offers and resets the reroll price. */
    public void openShop() {
        offers = shop.roll(player, wave);
        rerollCost = baseRerollCost();
    }

    private int baseRerollCost() {
        return 1 + wave;
    }

    public List<ShopOffer> getOffers() { return offers; }
    public int getRerollCost()         { return rerollCost; }

    public boolean reroll() {
        if (!player.spendMaterials(rerollCost)) return false;
        offers = shop.roll(player, wave);
        rerollCost += REROLL_INCREASE;
        return true;
    }

    public Player.PurchaseResult checkOffer(ShopOffer offer) {
        return offer.isWeapon() ? player.checkPurchase(offer.getWeapon())
                                : player.checkPurchase(offer.getItem());
    }

    public Player.PurchaseResult buyOffer(ShopOffer offer) {
        if (offer.isSold()) return Player.PurchaseResult.MAX_OWNED;
        Player.PurchaseResult result = offer.isWeapon() ? player.buy(offer.getWeapon())
                                                        : player.buy(offer.getItem());
        if (result == Player.PurchaseResult.SUCCESS) offer.markSold();
        return result;
    }

    // ---------------- Stats for the end screen ----------------

    public void addKill()                      { kills++; }
    public void addMaterialsCollected(int n)   { materialsCollected += n; }
    public int getKills()                      { return kills; }
    public int getMaterialsCollected()         { return materialsCollected; }

    public Player getPlayer()  { return player; }
    public Shop getShop()      { return shop; }
    public Random getRandom()  { return rng; }
}

package game.ui;

import game.GameSession;
import game.core.GamePanel;
import game.core.Screen;
import game.items.Item;
import game.items.Player;
import game.items.ShopOffer;
import game.items.Stat;
import game.weapons.Weapon;
import game.weapons.WeaponDef;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Between-wave shop: buy items/weapons, reroll, sell weapons, see stats.
 *
 * Layout (1280 x 720):
 *   left  - 4 offer cards, reroll button, weapon slots, owned items
 *   right - stats panel and "Start wave" button
 */
public class ShopScreen implements Screen {
    private static final int LEFT = 24;
    private static final int CARD_W = 212, CARD_H = 300, CARD_GAP = 14, CARD_Y = 96;
    private static final int SLOT_W = 142, SLOT_H = 86, SLOT_GAP = 10, SLOT_Y = 500;
    private static final int ITEM_SIZE = 40, ITEM_GAP = 5, ITEM_Y = 632;
    private static final int STATS_X = 944, STATS_Y = 24, STATS_W = 312, STATS_H = 600;

    private final GamePanel game;
    private final GameSession session;
    private final Player player;

    private final Button rerollButton = new Button(LEFT, 412, 200, 40, "");
    private final Button nextButton = new Button(STATS_X, 640, STATS_W, 56, "");

    private String message;
    private Color messageColor = UiTheme.TEXT;
    private double messageTimer;

    public ShopScreen(GamePanel game, GameSession session) {
        this.game = game;
        this.session = session;
        this.player = session.getPlayer();
        nextButton.color = UiTheme.GOOD;
        rerollButton.color = new Color(120, 170, 255);
    }

    // =====================================================================
    // Layout helpers (used by both drawing and clicking, so they always match)
    // =====================================================================

    private int cardX(int i)      { return LEFT + i * (CARD_W + CARD_GAP); }
    private int slotX(int i)      { return LEFT + i * (SLOT_W + SLOT_GAP); }
    private int itemX(int i)      { return LEFT + i * (ITEM_SIZE + ITEM_GAP); }

    private Button buyButton(int i) {
        return new Button(cardX(i) + 14, CARD_Y + CARD_H - 50, CARD_W - 28, 38, "");
    }

    private Button sellButton(int i) {
        return new Button(slotX(i) + 8, SLOT_Y + SLOT_H - 30, SLOT_W - 16, 24, "");
    }

    // =====================================================================
    // Update & input
    // =====================================================================

    @Override
    public void update(double dt) {
        if (messageTimer > 0) messageTimer -= dt;
    }

    private void showMessage(String text, Color color) {
        message = text;
        messageColor = color;
        messageTimer = 2.0;
    }

    @Override
    public void onClick(int x, int y) {
        List<ShopOffer> offers = session.getOffers();
        for (int i = 0; i < offers.size(); i++) {
            ShopOffer offer = offers.get(i);
            if (!offer.isSold() && buyButton(i).contains(x, y)) {
                Player.PurchaseResult r = session.buyOffer(offer);
                if (r == Player.PurchaseResult.SUCCESS) showMessage("Bought " + offer.getName(), UiTheme.GOOD);
                else showMessage(reasonText(r), UiTheme.BAD);
                return;
            }
        }

        List<Weapon> weapons = player.getWeapons();
        for (int i = 0; i < weapons.size(); i++) {
            if (sellButton(i).contains(x, y)) {
                WeaponDef def = weapons.get(i).getDef();
                if (player.sellWeapon(i)) {
                    showMessage("Sold " + def.getName() + " for " + def.getSellPrice(), UiTheme.ACCENT);
                } else {
                    showMessage("You need at least one weapon", UiTheme.BAD);
                }
                return;
            }
        }

        if (rerollButton.contains(x, y)) {
            if (session.reroll()) showMessage("Rerolled", UiTheme.TEXT);
            else showMessage("Not enough materials", UiTheme.BAD);
        } else if (nextButton.contains(x, y)) {
            game.startNextWave();
        }
    }

    private static String reasonText(Player.PurchaseResult r) {
        switch (r) {
            case NOT_ENOUGH_MATERIALS: return "Not enough materials";
            case MAX_OWNED:            return "You already own the max";
            case WEAPONS_FULL:         return "Weapon slots full (sell one first)";
            default:                   return "";
        }
    }

    // =====================================================================
    // Drawing
    // =====================================================================

    @Override
    public void render(Graphics2D g) {
        int mx = game.getInput().getMouseX(), my = game.getInput().getMouseY();
        g.setColor(UiTheme.BG);
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        drawHeader(g);
        List<ShopOffer> offers = session.getOffers();
        for (int i = 0; i < offers.size(); i++) drawCard(g, i, offers.get(i), mx, my);

        rerollButton.label = "Reroll  (" + session.getRerollCost() + ")";
        rerollButton.enabled = player.getMaterials() >= session.getRerollCost();
        rerollButton.draw(g, mx, my);
        if (message != null && messageTimer > 0) {
            g.setFont(UiTheme.BODY_BOLD);
            g.setColor(UiTheme.withAlpha(messageColor, Math.min(1, messageTimer)));
            g.drawString(message, LEFT + 220, 438);
        }

        drawWeapons(g, mx, my);
        drawItems(g);
        drawStats(g);

        nextButton.label = "START WAVE " + (session.getWave() + 1) + "  >";
        nextButton.draw(g, mx, my);

        drawHoverTooltip(g, mx, my);
    }

    private void drawHeader(Graphics2D g) {
        g.setFont(UiTheme.HEADER);
        g.setColor(UiTheme.ACCENT);
        g.drawString("SHOP", LEFT, 52);
        g.setFont(UiTheme.BODY);
        g.setColor(UiTheme.TEXT_DIM);
        g.drawString("Wave " + session.getWave() + " complete.  Spend your materials!", LEFT, 78);

        // Materials, right-aligned above the last card
        String mats = String.valueOf(player.getMaterials());
        g.setFont(UiTheme.HEADER);
        int right = cardX(3) + CARD_W;
        FontMetrics fm = g.getFontMetrics();
        int textX = right - fm.stringWidth(mats);
        g.setColor(UiTheme.TEXT);
        g.drawString(mats, textX, 56);
        UiTheme.drawMaterialIcon(g, textX - 20, 45, 11);
    }

    private void drawCard(Graphics2D g, int i, ShopOffer offer, int mx, int my) {
        int x = cardX(i), y = CARD_Y;
        Color tierColor = UiTheme.tierColor(offer.getTier());

        if (offer.isSold()) {
            UiTheme.drawPanel(g, x, y, CARD_W, CARD_H, new Color(35, 31, 42), UiTheme.BORDER, 2f);
            g.setFont(UiTheme.HEADER);
            g.setColor(UiTheme.TEXT_DIM);
            UiTheme.drawCentered(g, "SOLD", x + CARD_W / 2.0, y + CARD_H / 2.0 + 10);
            return;
        }

        UiTheme.drawPanel(g, x, y, CARD_W, CARD_H, UiTheme.PANEL, tierColor, 2.5f);
        g.setColor(tierColor);
        g.fillRoundRect(x + 2, y + 2, CARD_W - 4, 8, 6, 6);

        Color iconBorder = offer.isWeapon() ? offer.getWeapon().getColor() : tierColor;
        Icons.draw(g, offer.getIconPath(), offer.getName(), iconBorder, x + (CARD_W - 72) / 2, y + 22, 72);

        g.setFont(UiTheme.SUBHEADER);
        g.setColor(UiTheme.TEXT);
        UiTheme.drawCentered(g, offer.getName(), x + CARD_W / 2.0, y + 122);
        g.setFont(UiTheme.SMALL);
        g.setColor(tierColor);
        String kind = offer.isWeapon() ? "Weapon" : "Tier " + offer.getTier() + " item";
        UiTheme.drawCentered(g, kind, x + CARD_W / 2.0, y + 142);

        // Description, wrapped to fit
        g.setFont(UiTheme.SMALL);
        FontMetrics fm = g.getFontMetrics();
        int ly = y + 170;
        List<String> lines = offer.getDescriptionLines();
        for (int k = 0; k < lines.size(); k++) {
            String line = lines.get(k);
            boolean isWeaponType = offer.isWeapon() && k == 0;
            for (String part : UiTheme.wrap(line, fm, CARD_W - 28)) {
                if (ly > y + CARD_H - 70) break;
                g.setColor(isWeaponType ? UiTheme.TEXT_DIM : UiTheme.modifierColor(line));
                UiTheme.drawCentered(g, part, x + CARD_W / 2.0, ly);
                ly += 17;
            }
        }

        // Owned count
        int owned = offer.isWeapon() ? countWeapon(offer.getWeapon()) : player.countOf(offer.getItem());
        if (owned > 0) {
            g.setFont(UiTheme.SMALL);
            g.setColor(UiTheme.TEXT_DIM);
            UiTheme.drawCentered(g, "Owned: " + owned, x + CARD_W / 2.0, y + CARD_H - 60);
        }

        // Buy button
        Button buy = buyButton(i);
        Player.PurchaseResult check = session.checkOffer(offer);
        switch (check) {
            case MAX_OWNED:    buy.label = "Max owned"; break;
            case WEAPONS_FULL: buy.label = "Weapons full"; break;
            default:           buy.label = "Buy   " + offer.getPrice(); break;
        }
        buy.enabled = check == Player.PurchaseResult.SUCCESS;
        buy.draw(g, mx, my);
    }

    private int countWeapon(WeaponDef def) {
        int n = 0;
        for (Weapon w : player.getWeapons()) if (w.getDef() == def) n++;
        return n;
    }

    private void drawWeapons(Graphics2D g, int mx, int my) {
        List<Weapon> weapons = player.getWeapons();
        g.setFont(UiTheme.SUBHEADER);
        g.setColor(UiTheme.TEXT);
        g.drawString("WEAPONS  " + weapons.size() + " / " + Player.MAX_WEAPONS, LEFT, SLOT_Y - 12);

        for (int i = 0; i < Player.MAX_WEAPONS; i++) {
            int x = slotX(i);
            if (i >= weapons.size()) {
                g.setColor(UiTheme.BORDER);
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1f, new float[] { 6f, 6f }, 0f));
                g.drawRoundRect(x, SLOT_Y, SLOT_W, SLOT_H, 14, 14);
                g.setFont(UiTheme.SMALL);
                g.setColor(UiTheme.TEXT_DIM);
                UiTheme.drawCentered(g, "empty", x + SLOT_W / 2.0, SLOT_Y + SLOT_H / 2.0 + 5);
                continue;
            }
            WeaponDef def = weapons.get(i).getDef();
            UiTheme.drawPanel(g, x, SLOT_Y, SLOT_W, SLOT_H);
            Icons.draw(g, def.getIconPath(), def.getName(), def.getColor(), x + 8, SLOT_Y + 8, 40);
            g.setFont(UiTheme.SMALL_BOLD);
            g.setColor(UiTheme.TEXT);
            g.drawString(def.getName(), x + 56, SLOT_Y + 26);
            g.setFont(UiTheme.SMALL);
            g.setColor(UiTheme.TEXT_DIM);
            g.drawString(def.isMelee() ? "Melee" : "Ranged", x + 56, SLOT_Y + 44);

            Button sell = sellButton(i);
            sell.label = "Sell  +" + def.getSellPrice();
            sell.color = new Color(150, 140, 165);
            sell.enabled = weapons.size() > 1;
            sell.draw(g, mx, my);
        }
    }

    private void drawItems(Graphics2D g) {
        List<Item> unique = player.getUniqueItems();
        g.setFont(UiTheme.SUBHEADER);
        g.setColor(UiTheme.TEXT);
        g.drawString("ITEMS  " + player.getInventory().size(), LEFT, ITEM_Y - 12);
        if (unique.isEmpty()) {
            g.setFont(UiTheme.SMALL);
            g.setColor(UiTheme.TEXT_DIM);
            g.drawString("No items yet. Buy some above!", LEFT, ITEM_Y + 24);
        }
        for (int i = 0; i < unique.size(); i++) {
            Item item = unique.get(i);
            int x = itemX(i);
            Icons.draw(g, item.getIconPath(), item.getName(), UiTheme.tierColor(item.getTier()),
                    x, ITEM_Y, ITEM_SIZE);
            int count = player.countOf(item);
            if (count > 1) {
                g.setFont(UiTheme.SMALL_BOLD);
                UiTheme.drawOutlined(g, "x" + count, x + ITEM_SIZE - 18, ITEM_Y + ITEM_SIZE - 3, UiTheme.TEXT);
            }
        }
    }

    private void drawStats(Graphics2D g) {
        UiTheme.drawPanel(g, STATS_X, STATS_Y, STATS_W, STATS_H);
        g.setFont(UiTheme.SUBHEADER);
        g.setColor(UiTheme.ACCENT);
        g.drawString("STATS", STATS_X + 18, STATS_Y + 34);
        g.setFont(UiTheme.BODY);
        g.setColor(UiTheme.TEXT_DIM);
        UiTheme.drawRight(g, "Level " + player.getLevel(), STATS_X + STATS_W - 18, STATS_Y + 34);

        int y = STATS_Y + 72;
        for (Stat s : Stat.values()) {
            int v = player.getStat(s);
            g.setFont(UiTheme.BODY);
            g.setColor(UiTheme.TEXT);
            g.drawString(s.getDisplayName(), STATS_X + 18, y);
            g.setFont(UiTheme.BODY_BOLD);
            Color c = s == Stat.MAX_HP ? UiTheme.TEXT : v > 0 ? UiTheme.GOOD : v < 0 ? UiTheme.BAD : UiTheme.TEXT;
            g.setColor(c);
            UiTheme.drawRight(g, v + (s.isPercent() ? "%" : ""), STATS_X + STATS_W - 18, y);
            y += 34;
        }

        // A couple of useful derived numbers
        y += 4;
        g.setFont(UiTheme.SMALL);
        g.setColor(UiTheme.TEXT_DIM);
        int reduction = (int) Math.round((1 - player.getDamageTakenMultiplier()) * 100);
        String taken = reduction == 0 ? "0%" : (reduction > 0 ? "-" : "+") + Math.abs(reduction) + "%";
        g.drawString("Damage taken from enemies: " + taken, STATS_X + 18, y);
    }

    /** Shows full details when hovering an owned item or weapon. */
    private void drawHoverTooltip(Graphics2D g, int mx, int my) {
        List<Item> unique = player.getUniqueItems();
        for (int i = 0; i < unique.size(); i++) {
            int x = itemX(i);
            if (mx >= x && mx <= x + ITEM_SIZE && my >= ITEM_Y && my <= ITEM_Y + ITEM_SIZE) {
                Item item = unique.get(i);
                List<String> lines = new ArrayList<>(item.getDescriptionLines());
                lines.add("Owned: " + player.countOf(item));
                UiTheme.drawTooltip(g, item.getName(), UiTheme.tierColor(item.getTier()), lines,
                        mx, my - 150, GamePanel.WIDTH, GamePanel.HEIGHT);
                return;
            }
        }
        List<Weapon> weapons = player.getWeapons();
        for (int i = 0; i < weapons.size(); i++) {
            int x = slotX(i);
            boolean inSlot = mx >= x && mx <= x + SLOT_W && my >= SLOT_Y && my <= SLOT_Y + SLOT_H;
            if (inSlot && !sellButton(i).contains(mx, my)) {
                WeaponDef def = weapons.get(i).getDef();
                UiTheme.drawTooltip(g, def.getName(), def.getColor(), def.getDescriptionLines(),
                        mx, my - 160, GamePanel.WIDTH, GamePanel.HEIGHT);
                return;
            }
        }
    }
}

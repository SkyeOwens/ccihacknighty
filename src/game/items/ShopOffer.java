package game.items;

import game.weapons.WeaponDef;

import java.util.List;

/** One card in the shop: either an Item or a Weapon. */
public class ShopOffer {
    private final Item item;
    private final WeaponDef weapon;
    private boolean sold;

    private ShopOffer(Item item, WeaponDef weapon) {
        this.item = item;
        this.weapon = weapon;
    }

    public static ShopOffer of(Item item)       { return new ShopOffer(item, null); }
    public static ShopOffer of(WeaponDef weapon) { return new ShopOffer(null, weapon); }

    public boolean isWeapon()     { return weapon != null; }
    public Item getItem()         { return item; }
    public WeaponDef getWeapon()  { return weapon; }
    public boolean isSold()       { return sold; }
    public void markSold()        { sold = true; }

    public String getName()       { return isWeapon() ? weapon.getName() : item.getName(); }
    public int getPrice()         { return isWeapon() ? weapon.getPrice() : item.getPrice(); }
    /** Weapons are all tier 1 for now. */
    public int getTier()          { return isWeapon() ? 1 : item.getTier(); }
    public String getIconPath()   { return isWeapon() ? weapon.getIconPath() : item.getIconPath(); }

    public List<String> getDescriptionLines() {
        return isWeapon() ? weapon.getDescriptionLines() : item.getDescriptionLines();
    }
}

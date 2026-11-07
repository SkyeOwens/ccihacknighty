package game.weapons;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Every weapon in the game. To add one, define it here and add it to ALL. */
public final class Weapons {
    private Weapons() {}

    public static final WeaponDef FIST = WeaponDef.builder("fist", "Fist", 15, true)
            .damage(8).cooldown(0.9).range(110)
            .color(new Color(240, 200, 160)).build();

    public static final WeaponDef KNIFE = WeaponDef.builder("knife", "Knife", 20, true)
            .damage(6).cooldown(0.6).range(140)
            .color(new Color(210, 215, 225)).build();

    public static final WeaponDef SPEAR = WeaponDef.builder("spear", "Spear", 30, true)
            .damage(14).cooldown(1.4).range(240)
            .color(new Color(170, 120, 70)).build();

    public static final WeaponDef PISTOL = WeaponDef.builder("pistol", "Pistol", 25, false)
            .damage(10).cooldown(1.1).range(380).projectileSpeed(750).pierce(1)
            .color(new Color(255, 220, 90)).build();

    public static final WeaponDef SMG = WeaponDef.builder("smg", "SMG", 30, false)
            .damage(3).cooldown(0.2).range(320).projectileSpeed(800).spreadDegrees(14)
            .color(new Color(255, 170, 60)).build();

    public static final WeaponDef SHOTGUN = WeaponDef.builder("shotgun", "Shotgun", 35, false)
            .damage(4).cooldown(1.3).range(260).projectileSpeed(650)
            .projectiles(5).spreadDegrees(32).pierce(1)
            .color(new Color(255, 130, 80)).build();

    public static final List<WeaponDef> ALL = Collections.unmodifiableList(Arrays.asList(
            FIST, KNIFE, SPEAR, PISTOL, SMG, SHOTGUN));

    public static WeaponDef byId(String id) {
        for (WeaponDef w : ALL) {
            if (w.getId().equals(id)) return w;
        }
        return null;
    }
}

package game.items;

import java.util.Random;

/**
 * Special behaviour that plain stat numbers can't express (Piggy Bank, Adrenaline).
 * Override only the hooks you need. To add a new trigger (on kill, on crit, on
 * level up...), add a default method here and call it from Player.
 */
public interface ItemEffect {
    default void onWaveStart(Player player) {}

    default void onDodge(Player player, Random rng) {}
}

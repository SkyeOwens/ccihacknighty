package game.world;

import java.util.Random;

/** All the difficulty numbers in one place, so they are easy to tune. */
public final class WaveConfig {
    private WaveConfig() {}

    /** Wave length in seconds: 20s on wave 1, +4s per wave, capped at 60s. */
    public static double duration(int wave) {
        return Math.min(60, 20 + 4 * (wave - 1));
    }

    /** Seconds between enemy groups. */
    public static double spawnInterval(int wave) {
        return Math.max(0.55, 1.2 - 0.07 * (wave - 1));
    }

    public static int groupSize(int wave, EnemyType type, Random rng) {
        if (type == EnemyType.BRUISER) return 1 + rng.nextInt(1 + wave / 5);
        return 2 + rng.nextInt(1 + (wave + 1) / 3);
    }

    /** Picks a random enemy type that is allowed on this wave, using spawn weights. */
    public static EnemyType pickType(int wave, Random rng) {
        int total = 0;
        for (EnemyType t : EnemyType.values()) {
            if (t.getFirstWave() <= wave) total += t.getSpawnWeight();
        }
        int r = rng.nextInt(Math.max(1, total));
        for (EnemyType t : EnemyType.values()) {
            if (t.getFirstWave() > wave) continue;
            r -= t.getSpawnWeight();
            if (r < 0) return t;
        }
        return EnemyType.BABY;
    }

    /** Chance that a killed enemy drops a crate (contains a free item). Luck helps. */
    public static double crateChance(int luck) {
        return 0.015 * Math.max(0, 1.0 + luck / 100.0);
    }
}

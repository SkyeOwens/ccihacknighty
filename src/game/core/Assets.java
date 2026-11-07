package game.core;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Loads your PNG art. Put images in an "assets" folder next to where you run the
 * game (or inside src/assets so they end up on the classpath), for example:
 *
 *   assets/player.png
 *   assets/items/helmet.png      (the id of each item, see Items.java)
 *   assets/weapons/pistol.png    (the id of each weapon, see Weapons.java)
 *   assets/enemies/baby.png      (baby, charger, bruiser, boss)
 *
 * Any image that is missing is simply drawn as a shape instead, so the game
 * always runs even before all the art is done.
 */
public final class Assets {
    private Assets() {}

    private static final Map<String, BufferedImage> cache = new HashMap<>();
    private static final Set<String> missing = new HashSet<>();

    /** Returns the image, or null if it doesn't exist. Results are cached. */
    public static BufferedImage get(String path) {
        if (path == null || missing.contains(path)) return null;
        BufferedImage cached = cache.get(path);
        if (cached != null) return cached;

        BufferedImage img = null;
        try {
            File file = new File("assets", path);
            if (file.exists()) {
                img = ImageIO.read(file);
            } else {
                InputStream in = Assets.class.getResourceAsStream("/assets/" + path);
                if (in != null) {
                    try {
                        img = ImageIO.read(in);
                    } finally {
                        in.close();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load image " + path + ": " + e.getMessage());
        }

        if (img == null) {
            missing.add(path);
        } else {
            cache.put(path, img);
        }
        return img;
    }
}

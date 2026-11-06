package src.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable item definition. There is one Item object per item type (see
 * Items);
 * the player's inventory just holds references to them, so owning 3 Helmets
 * means
 * the list contains Items.HELMET three times.
 */
public class Item {
    public static final int UNLIMITED = 0;

    private final String id;
    private final String name;
    private final int tier;
    private final int price;
    private final int maxCount;
    private final Map<Stat, Integer> modifiers;
    private final ItemEffect effect; // null if the item only changes stats
    private final String effectText; // UI text for the special effect, or null
    private final String iconPath;

    private Item(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.tier = b.tier;
        this.price = b.price;
        this.maxCount = b.maxCount;
        this.modifiers = Collections.unmodifiableMap(new LinkedHashMap<>(b.modifiers));
        this.effect = b.effect;
        this.effectText = b.effectText;
        this.iconPath = b.iconPath != null ? b.iconPath : "items/" + b.id + ".png";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTier() {
        return tier;
    }

    public int getPrice() {
        return price;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public boolean isLimited() {
        return maxCount != UNLIMITED;
    }

    public Map<Stat, Integer> getModifiers() {
        return modifiers;
    }

    public ItemEffect getEffect() {
        return effect;
    }

    public boolean hasEffect() {
        return effect != null;
    }

    public String getIconPath() {
        return iconPath;
    }

    /**
     * Lines of text for a tooltip or shop card, e.g.
     * ["Max 1", "+5% Dodge", "50% chance to heal 5 HP when dodging"].
     */
    public List<String> getDescriptionLines() {
        List<String> lines = new ArrayList<>();
        if (isLimited()) {
            lines.add("Max " + maxCount);
        }
        for (Map.Entry<Stat, Integer> e : modifiers.entrySet()) {
            lines.add(e.getKey().format(e.getValue()));
        }
        if (effectText != null) {
            lines.add(effectText);
        }
        return lines;
    }

    public String getDescription() {
        return String.join(", ", getDescriptionLines());
    }

    @Override
    public String toString() {
        return name + " (T" + tier + ", " + price + "): " + getDescription();
    }

    public static Builder builder(String id, String name, int tier, int price) {
        return new Builder(id, name, tier, price);
    }

    public static class Builder {
        private final String id;
        private final String name;
        private final int tier;
        private final int price;
        private int maxCount = UNLIMITED;
        // LinkedHashMap keeps stats in the order you list them, so tooltips match your
        // design.
        private final LinkedHashMap<Stat, Integer> modifiers = new LinkedHashMap<>();
        private ItemEffect effect;
        private String effectText;
        private String iconPath;

        private Builder(String id, String name, int tier, int price) {
            this.id = id;
            this.name = name;
            this.tier = tier;
            this.price = price;
        }

        public Builder stat(Stat stat, int amount) {
            modifiers.merge(stat, amount, Integer::sum);
            return this;
        }

        public Builder max(int maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public Builder effect(String text, ItemEffect effect) {
            this.effectText = text;
            this.effect = effect;
            return this;
        }

        public Builder icon(String iconPath) {
            this.iconPath = iconPath;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }
}

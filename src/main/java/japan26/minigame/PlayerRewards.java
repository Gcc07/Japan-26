package japan26.minigame;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lightweight reward inventory for minigame unlocks.
 */
public final class PlayerRewards {
    private static final Set<String> unlockedItems = new LinkedHashSet<>();

    private PlayerRewards() {
    }

    public static void unlock(String itemName) {
        unlockedItems.add(itemName);
    }

    public static boolean has(String itemName) {
        return unlockedItems.contains(itemName);
    }

    public static Set<String> all() {
        return Collections.unmodifiableSet(unlockedItems);
    }
}

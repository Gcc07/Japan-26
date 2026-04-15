package japan26.minigame;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry that maps string keys to Minigame instances.
 * Register your minigames here so SceneManager can look them up by key.
 *
 * Example:
 *   MinigameRegistry.register("shrine_puzzle", new ShrinePuzzleMinigame());
 */
public class MinigameRegistry {

    private static final Map<String, Minigame> registry = new HashMap<>();

    public static void register(String key, Minigame game) {
        registry.put(key, game);
    }

    /** Returns the minigame for the given key, or null if not yet registered. */
    public static Minigame get(String key) {
        return registry.get(key);
    }
}

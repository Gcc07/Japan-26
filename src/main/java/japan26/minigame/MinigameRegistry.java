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

    /** Registers default placeholder minigames for integration testing. */
    public static void ensureDefaultTestMinigames() {
        registry.putIfAbsent("photo_focus", new RhythmDrumMinigame());
        registry.putIfAbsent("rhythm_game", new RhythmDrumMinigame());
        registry.putIfAbsent("claw_machine", new ClawMachineMinigame());
        registry.putIfAbsent("street_quicktime", new PlaceholderMinigame(
                "Street Quicktime",
                "Placeholder for a timing/reaction minigame during story transitions."
        ));
        registry.putIfAbsent("memory_snap", new PlaceholderMinigame(
                "Memory Snap",
                "Placeholder for your memory or pattern minigame."
        ));
    }

    /** Returns the minigame for the given key, or null if not yet registered. */
    public static Minigame get(String key) {
        return registry.get(key);
    }
}

package japan26.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Shared UI settings state.
 */
public final class SettingsState {

    public static final int PLAYER_PRESET_MIN = 1;
    public static final int PLAYER_PRESET_MAX = 11;

    private static final int[] LOCKED_BY_DEFAULT = {5, 6, 7, 11};

    private static int volumePercent = 70;
    private static boolean typewriterSfxEnabled = true;
    private static String playerName = "Traveler";
    /** 1-based index matching {@code /japan26/Sprites/player{n}.png}. */
    private static int playerPresetIndex = 1;
    /** Presets explicitly unlocked by story/progression (see {@link #unlockPlayerPreset}). */
    private static final Set<Integer> unlockedPresetOverrides = new HashSet<>();

    private SettingsState() {
    }

    public static int getVolumePercent() {
        return volumePercent;
    }

    public static void setVolumePercent(int value) {
        volumePercent = Math.max(0, Math.min(100, value));
    }

    public static boolean isMuted() {
        return volumePercent == 0;
    }

    public static boolean isTypewriterSfxEnabled() {
        return typewriterSfxEnabled;
    }

    public static void setTypewriterSfxEnabled(boolean enabled) {
        typewriterSfxEnabled = enabled;
    }

    public static String getPlayerName() {
        return playerName;
    }

    public static void setPlayerName(String name) {
        String trimmed = name == null ? "" : name.trim();
        playerName = trimmed.isEmpty() ? "Traveler" : trimmed;
    }

    public static int getPlayerPresetIndex() {
        return playerPresetIndex;
    }

    public static void setPlayerPresetIndex(int preset1Based) {
        playerPresetIndex = Math.max(PLAYER_PRESET_MIN, Math.min(PLAYER_PRESET_MAX, preset1Based));
    }

    /**
     * Resource path for the selected player portrait sprite (class-loader path).
     */
    public static String getPlayerSpriteResourcePath() {
        return "/japan26/Sprites/player" + playerPresetIndex + ".png";
    }

    public static boolean isPlayerPresetUnlocked(int preset1Based) {
        if (preset1Based < PLAYER_PRESET_MIN || preset1Based > PLAYER_PRESET_MAX) {
            return false;
        }
        for (int locked : LOCKED_BY_DEFAULT) {
            if (locked == preset1Based) {
                return unlockedPresetOverrides.contains(preset1Based);
            }
        }
        return true;
    }

    /**
     * Call from story/minigame rewards when the player earns a locked look.
     */
    public static void unlockPlayerPreset(int preset1Based) {
        if (preset1Based < PLAYER_PRESET_MIN || preset1Based > PLAYER_PRESET_MAX) {
            return;
        }
        if (Arrays.binarySearch(LOCKED_BY_DEFAULT, preset1Based) < 0) {
            return;
        }
        unlockedPresetOverrides.add(preset1Based);
    }

    /** First unlocked preset in ascending order (for safe defaults). */
    public static int firstUnlockedPlayerPreset() {
        for (int i = PLAYER_PRESET_MIN; i <= PLAYER_PRESET_MAX; i++) {
            if (isPlayerPresetUnlocked(i)) {
                return i;
            }
        }
        return PLAYER_PRESET_MIN;
    }
}

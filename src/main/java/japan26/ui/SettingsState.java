package japan26.ui;

/**
 * Shared UI settings state.
 */
public final class SettingsState {
    private static int volumePercent = 70;
    private static String playerName = "Traveler";

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

    public static String getPlayerName() {
        return playerName;
    }

    public static void setPlayerName(String name) {
        String trimmed = name == null ? "" : name.trim();
        playerName = trimmed.isEmpty() ? "Traveler" : trimmed;
    }
}

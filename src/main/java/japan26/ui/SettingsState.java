package japan26.ui;

/**
 * Shared UI settings state.
 */
public final class SettingsState {
    private static int volumePercent = 70;

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
}

package japan26.ui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.Toolkit;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Plays UI SFX from the bundled JDSherbert pack.
 */
public final class UISound {
    private static final ExecutorService SOUND_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ui-sfx");
        t.setDaemon(true);
        return t;
    });

    private static final Path[] SFX_BASES = new Path[] {
            Paths.get("src", "main", "java", "japan26", "SFX", "SFX pack", "Mono", "wav (SD)"),
            Paths.get("target", "classes", "japan26", "SFX", "SFX pack", "Mono", "wav (SD)")
    };

    private static final String HOVER_FILE  = "JDSherbert - Ultimate UI SFX Pack - Cursor - 2.wav";
    private static final String SELECT_FILE = "JDSherbert - Ultimate UI SFX Pack - Select - 1.wav";
    private static final String START_FILE  = "JDSherbert - Ultimate UI SFX Pack - Popup Open - 1.wav";

    private static long lastHoverMs = 0L;
    private static long lastTypewriterMs = 0L;

    private UISound() {
    }

    public static void playHover() {
        if (SettingsState.isMuted()) return;
        long now = System.currentTimeMillis();
        if (now - lastHoverMs < 90) return;
        lastHoverMs = now;
        play(HOVER_FILE, false);
    }

    public static void playSelect() {
        if (SettingsState.isMuted()) return;
        play(SELECT_FILE, true);
    }

    public static void playStart() {
        if (SettingsState.isMuted()) return;
        play(START_FILE, true);
    }

    public static void playTypewriterTick() {
        if (SettingsState.isMuted()) return;
        if (!SettingsState.isTypewriterSfxEnabled()) return;
        long now = System.currentTimeMillis();
        if (now - lastTypewriterMs < 40) return;
        lastTypewriterMs = now;
        play(HOVER_FILE, false);
    }

    private static void play(String fileName, boolean fallbackBeep) {
        SOUND_EXECUTOR.submit(() -> {
            boolean played = false;
            for (Path base : SFX_BASES) {
                Path path = base.resolve(fileName);
                if (!path.toFile().exists()) {
                    continue;
                }
                try (AudioInputStream stream = AudioSystem.getAudioInputStream(path.toFile())) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(stream);
                    applyVolume(clip, SettingsState.getVolumePercent());
                    clip.addLineListener(e -> {
                        if (e.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                    clip.start();
                    played = true;
                    break;
                } catch (Exception ignored) {
                    // Try next base path
                }
            }
            if (!played && fallbackBeep) {
                try {
                    Toolkit.getDefaultToolkit().beep();
                } catch (Exception ignored) {
                    // No-op fallback
                }
            }
        });
    }

    private static void applyVolume(Clip clip, int volumePercent) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (volumePercent <= 0) {
                gain.setValue(gain.getMinimum());
                return;
            }
            float min = gain.getMinimum();
            float max = gain.getMaximum();
            float normalized = volumePercent / 100f;
            float dB = (float) (20.0 * Math.log10(normalized));
            gain.setValue(Math.max(min, Math.min(max, dB)));
        } catch (Exception ignored) {
            // Some mixers/clips may not expose MASTER_GAIN
        }
    }
}

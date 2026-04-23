package japan26.engine;

import japan26.minigame.Minigame;
import japan26.minigame.MinigameRegistry;
import japan26.model.StoryScene;
import japan26.story.MinigameTestStory;
import japan26.story.StoryData;
import japan26.story.TestStory;
import japan26.ui.GameView;
import japan26.ui.MainMenuView;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Central hub for switching between the main Swing screens:
 *   Main Menu  →  Story / Game  →  Minigame  →  Story / Game  → ...
 *
 * Call SceneManager.init() once at startup, then use the static helpers.
 */
public class SceneManager {

    private static final int FADE_DURATION_MS = 360;

    private static JFrame     primaryFrame;
    private static StoryEngine storyEngine;
    private static boolean     firstMenuShow = true;

    // ── Initialisation ────────────────────────────────────────────────────────

    public static void init() {
        primaryFrame = new JFrame("Japan 26");
        storyEngine  = new StoryEngine();
        MinigameRegistry.ensureDefaultTestMinigames();
        primaryFrame.setSize(1280, 720);
        primaryFrame.setResizable(false);
        primaryFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        primaryFrame.setLocationRelativeTo(null);
    }

    // ── Screen transitions ────────────────────────────────────────────────────

    public static void showMainMenu() {
        MainMenuView menu = new MainMenuView();
        primaryFrame.setContentPane(menu);
        primaryFrame.revalidate();
        primaryFrame.repaint();
        primaryFrame.setVisible(true);
        if (firstMenuShow) {
            firstMenuShow = false;
            playFadeIn();
        }
    }

    public static void startGame() {
        startGame(StoryData.buildStory(), false);
    }

    public static void startGameWithNamePrompt() {
        startGame(StoryData.buildStory(), true);
    }

    public static void startTestStory() {
        startGame(TestStory.buildStory(), false);
    }

    public static void startMinigameTestStory() {
        startGame(MinigameTestStory.buildStory(), false);
    }

    public static void startGame(List<StoryScene> story) {
        startGame(story, false);
    }

    public static void startGame(List<StoryScene> story, boolean askNameAfterFade) {
        playSceneTransition(() -> {
            GameView gameView = new GameView(storyEngine, story, askNameAfterFade);
            primaryFrame.setContentPane(gameView);
            primaryFrame.revalidate();
            primaryFrame.repaint();
        });
    }

    /** Return to the already-running story state (used after minigames). */
    private static void resumeCurrentStoryView() {
        playSceneTransition(() -> {
            GameView gameView = new GameView(storyEngine, List.of(), false, true);
            primaryFrame.setContentPane(gameView);
            primaryFrame.revalidate();
            primaryFrame.repaint();
        });
    }

    /**
     * Launches the minigame registered under {@code key}.
     * When the minigame signals completion it calls {@code onComplete}.
     */
    public static void launchMinigame(String key, Runnable onComplete) {
        Minigame game = MinigameRegistry.get(key);
        if (game == null) {
            // No minigame registered yet – skip straight to callback
            onComplete.run();
            return;
        }
        game.setOnComplete(() -> {
            onComplete.run();
            resumeCurrentStoryView();
        });
        game.start(primaryFrame);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public static JFrame      getFrame()       { return primaryFrame; }
    public static StoryEngine getStoryEngine() { return storyEngine; }

    // ── Fade helpers ───────────────────────────────────────────────────────────

    private static void playFadeIn() {
        FadeOverlay overlay = new FadeOverlay();
        primaryFrame.setGlassPane(overlay);
        overlay.setVisible(true);
        animateOverlay(overlay, 1f, 0f, FADE_DURATION_MS, () -> overlay.setVisible(false));
    }

    private static void playSceneTransition(Runnable swapScene) {
        FadeOverlay overlay = new FadeOverlay();
        primaryFrame.setGlassPane(overlay);
        overlay.setVisible(true);
        animateOverlay(overlay, 0f, 1f, FADE_DURATION_MS / 2, () -> {
            swapScene.run();
            animateOverlay(overlay, 1f, 0f, FADE_DURATION_MS / 2, () -> overlay.setVisible(false));
        });
    }

    private static void animateOverlay(
            FadeOverlay overlay,
            float fromAlpha,
            float toAlpha,
            int durationMs,
            Runnable onDone
    ) {
        long start = System.currentTimeMillis();
        overlay.setAlpha(fromAlpha);
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            float t = (System.currentTimeMillis() - start) / (float) durationMs;
            t = Math.max(0f, Math.min(1f, t));
            float eased = t * t * (3f - 2f * t); // smoothstep easing
            overlay.setAlpha(fromAlpha + (toAlpha - fromAlpha) * eased);
            if (t >= 1f) {
                timer.stop();
                if (onDone != null) onDone.run();
            }
        });
        timer.start();
    }

    private static class FadeOverlay extends JPanel {
        private float alpha = 1f;

        FadeOverlay() {
            setOpaque(false);
        }

        void setAlpha(float alpha) {
            this.alpha = Math.max(0f, Math.min(1f, alpha));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0f, 0f, 0f, alpha));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}

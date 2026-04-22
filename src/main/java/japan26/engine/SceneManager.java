package japan26.engine;

import japan26.minigame.Minigame;
import japan26.minigame.MinigameRegistry;
import japan26.model.StoryScene;
import japan26.story.StoryData;
import japan26.story.TestStory;
import japan26.ui.GameView;
import japan26.ui.MainMenuView;

import javax.swing.JFrame;
import java.util.List;

/**
 * Central hub for switching between the main Swing screens:
 *   Main Menu  →  Story / Game  →  Minigame  →  Story / Game  → ...
 *
 * Call SceneManager.init() once at startup, then use the static helpers.
 */
public class SceneManager {

    private static JFrame     primaryFrame;
    private static StoryEngine storyEngine;

    // ── Initialisation ────────────────────────────────────────────────────────

    public static void init() {
        primaryFrame = new JFrame("Japan 26");
        storyEngine  = new StoryEngine();
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
    }

    public static void startGame() {
        startGame(StoryData.buildStory());
    }

    public static void startTestStory() {
        startGame(TestStory.buildStory());
    }

    public static void startGame(List<StoryScene> story) {
        GameView gameView = new GameView(storyEngine, story);
        primaryFrame.setContentPane(gameView);
        primaryFrame.revalidate();
        primaryFrame.repaint();
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
            startGame(); // return to story view
        });
        game.start(primaryFrame);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public static JFrame      getFrame()       { return primaryFrame; }
    public static StoryEngine getStoryEngine() { return storyEngine; }
}

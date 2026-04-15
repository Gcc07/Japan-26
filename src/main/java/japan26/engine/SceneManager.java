package japan26.engine;

import japan26.minigame.Minigame;
import japan26.minigame.MinigameRegistry;
import japan26.ui.GameView;
import japan26.ui.MainMenuView;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central hub for switching between the main JavaFX Scenes (screens):
 *   Main Menu  →  Story / Game  →  Minigame  →  Story / Game  → ...
 *
 * Call SceneManager.init(stage) once at startup, then use the static helpers.
 */
public class SceneManager {

    private static Stage      primaryStage;
    private static StoryEngine storyEngine;

    // ── Initialisation ────────────────────────────────────────────────────────

    public static void init(Stage stage) {
        primaryStage = stage;
        storyEngine  = new StoryEngine();

        stage.setTitle("Japan 26");
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setResizable(false);
    }

    // ── Screen transitions ────────────────────────────────────────────────────

    public static void showMainMenu() {
        MainMenuView menu = new MainMenuView();
        primaryStage.setScene(new Scene(menu, 1280, 720));
        primaryStage.show();
    }

    public static void startGame() {
        GameView gameView = new GameView(storyEngine);
        primaryStage.setScene(new Scene(gameView, 1280, 720));
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
        game.start(primaryStage);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public static Stage       getStage()       { return primaryStage; }
    public static StoryEngine getStoryEngine() { return storyEngine; }
}

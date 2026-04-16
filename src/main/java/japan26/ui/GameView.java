package japan26.ui;

import japan26.engine.SceneManager;
import japan26.engine.StoryEngine;
import japan26.model.DialogueLine;
import japan26.model.StoryScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Main game screen: a full-window StackPane with a background image layer and
 * a DialogueBox anchored to the bottom.
 *
 * Click anywhere (or press Space / Enter) to advance dialogue.
 */
public class GameView extends StackPane {

    private static final String FALLBACK_BG = "/japan26/images/Skyline.jpg";

    private final StoryEngine engine;
    private final ImageView   background;
    private final DialogueBox dialogueBox;

    public GameView(StoryEngine engine, java.util.List<StoryScene> story) {
        this.engine = engine;

        // ── Background image layer ─────────────────────────────────────────
        background = new ImageView();
        background.setPreserveRatio(false);
        background.setFitWidth(1280);
        background.setFitHeight(720);

        // ── Dialogue box pinned to the bottom ──────────────────────────────
        dialogueBox = new DialogueBox();
        dialogueBox.setMaxWidth(1200);
        dialogueBox.setMinHeight(160);

        VBox bottomBar = new VBox(dialogueBox);
        bottomBar.setAlignment(Pos.BOTTOM_CENTER);
        bottomBar.setPadding(new Insets(0, 40, 30, 40));

        getChildren().addAll(background, bottomBar);
        StackPane.setAlignment(bottomBar, Pos.BOTTOM_CENTER);

        // ── Input ──────────────────────────────────────────────────────────
        setOnMouseClicked(e -> handleAdvance());
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                handleAdvance();
            }
        });

        // ── Wire story engine callbacks ────────────────────────────────────
        engine.setOnLineChanged(this::refreshLine);
        engine.setOnSceneChanged(this::refreshScene);
        engine.setOnStoryFinished(this::onStoryDone);

        // ── Stylesheet ────────────────────────────────────────────────────
        var css = getClass().getResource("/japan26/css/style.css");
        if (css != null) getStylesheets().add(css.toExternalForm());

        // ── Load and start the story ───────────────────────────────────────
        engine.loadStory(story);
        requestFocus();
    }

    // ── Advance logic ─────────────────────────────────────────────────────────

    private void handleAdvance() {
        if (dialogueBox.isTyping()) {
            dialogueBox.skipTyping();
        } else {
            engine.advance();
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void refreshScene() {
        StoryScene scene = engine.getCurrentScene();
        setBackground(scene.getDefaultBackground());
        refreshLine();
    }

    private void refreshLine() {
        DialogueLine line = engine.getCurrentLine();
        if (line == null) return;
        if (line.changesBackground()) setBackground(line.getBackgroundPath());
        dialogueBox.show(line);
    }

    private void setBackground(String resourcePath) {
        try {
            var url = getClass().getResource(resourcePath);
            if (url == null) url = getClass().getResource(FALLBACK_BG);
            if (url != null) background.setImage(new Image(url.toExternalForm()));
        } catch (Exception ignored) {
            // Background stays as-is if the image is missing
        }
    }

    private void onStoryDone() {
        SceneManager.showMainMenu();
    }
}

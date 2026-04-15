package japan26.ui;

import japan26.model.Character;
import japan26.model.DialogueLine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * The translucent text box shown at the bottom of the screen.
 * Displays the speaking character's name and their dialogue with a
 * typewriter reveal effect.
 *
 * Call show(line) to display a new line.
 * Call skipTyping() to reveal the full text immediately (e.g. on click).
 * isTyping() tells the GameView whether to skip or advance.
 */
public class DialogueBox extends VBox {

    private static final double CHAR_DELAY_MS = 25; // ms per character

    private final Label nameLabel;
    private final Label textLabel;
    private final Label continueHint;

    private Timeline typewriterTimeline;
    private String   fullText  = "";
    private boolean  typing    = false;

    public DialogueBox() {
        getStyleClass().add("dialogue-box");
        setSpacing(6);
        setPadding(new Insets(20, 30, 16, 30));

        nameLabel    = new Label();
        nameLabel.getStyleClass().add("character-name");

        textLabel    = new Label();
        textLabel.getStyleClass().add("dialogue-text");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(Double.MAX_VALUE);

        continueHint = new Label("▼");
        continueHint.getStyleClass().add("continue-hint");
        continueHint.setVisible(false);

        getChildren().addAll(nameLabel, textLabel, continueHint);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void show(DialogueLine line) {
        Character ch = line.getCharacter();

        boolean isNarrator = ch.getName().isEmpty();
        nameLabel.setText(isNarrator ? "" : ch.getName());
        nameLabel.setStyle("-fx-text-fill: " + ch.getColor() + ";");
        nameLabel.setVisible(!isNarrator);

        textLabel.getStyleClass().removeAll("dialogue-text", "narrator-text");
        textLabel.getStyleClass().add(isNarrator ? "narrator-text" : "dialogue-text");

        startTypewriter(line.getText());
    }

    public boolean isTyping() { return typing; }

    public void skipTyping() {
        if (!typing) return;
        if (typewriterTimeline != null) typewriterTimeline.stop();
        textLabel.setText(fullText);
        typing = false;
        continueHint.setVisible(true);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void startTypewriter(String text) {
        if (typewriterTimeline != null) typewriterTimeline.stop();

        fullText = text;
        typing   = true;
        continueHint.setVisible(false);
        textLabel.setText("");

        typewriterTimeline = new Timeline();
        for (int i = 1; i <= text.length(); i++) {
            final int idx = i;
            typewriterTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(CHAR_DELAY_MS * i),
                    e -> textLabel.setText(text.substring(0, idx)))
            );
        }
        typewriterTimeline.setOnFinished(e -> {
            typing = false;
            continueHint.setVisible(true);
        });
        typewriterTimeline.play();
    }
}

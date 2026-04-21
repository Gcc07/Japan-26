package japan26.ui;

import japan26.model.Character;
import japan26.model.DialogueLine;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;

/**
 * The translucent text box shown at the bottom of the screen.
 * Displays the speaking character's name and their dialogue with a
 * typewriter reveal effect.
 *
 * Call show(line) to display a new line.
 * Call skipTyping() to reveal the full text immediately (e.g. on click).
 * isTyping() tells the GameView whether to skip or advance.
 */
public class DialogueBox extends JPanel {

    private static final double CHAR_DELAY_MS = 25; // ms per character

    private final JLabel nameLabel;
    private final JLabel textLabel;
    private final JLabel continueHint;

    private Timer    typewriterTimer;
    private String   fullText  = "";
    private boolean  typing    = false;

    public DialogueBox() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(new Color(24, 20, 28, 200));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 16, 30));

        nameLabel = new JLabel("");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        textLabel = new JLabel("");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        textLabel.setForeground(Color.WHITE);

        continueHint = new JLabel("\u25BE");
        continueHint.setHorizontalAlignment(SwingConstants.RIGHT);
        continueHint.setVisible(false);

        add(nameLabel);
        add(textLabel);
        add(continueHint);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void show(DialogueLine line) {
        Character ch = line.getCharacter();

        boolean isNarrator = ch.getName().isEmpty();
        nameLabel.setText(isNarrator ? "" : ch.getName());
        nameLabel.setForeground(Color.decode(ch.getColor()));
        nameLabel.setVisible(!isNarrator);
        textLabel.setForeground(isNarrator ? new Color(235, 235, 235) : Color.WHITE);

        startTypewriter(line.getText());
    }

    public boolean isTyping() { return typing; }

    public void skipTyping() {
        if (!typing) return;
        if (typewriterTimer != null) typewriterTimer.stop();
        textLabel.setText(toHtml(fullText));
        typing = false;
        continueHint.setVisible(true);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void startTypewriter(String text) {
        if (typewriterTimer != null) typewriterTimer.stop();

        fullText = text;
        typing   = true;
        continueHint.setVisible(false);
        textLabel.setText("");

        final int[] idx = {0};
        typewriterTimer = new Timer((int) CHAR_DELAY_MS, e -> {
            idx[0]++;
            if (idx[0] >= fullText.length()) {
                textLabel.setText(toHtml(fullText));
                typing = false;
                continueHint.setVisible(true);
                typewriterTimer.stop();
                return;
            }
            textLabel.setText(toHtml(fullText.substring(0, idx[0])));
        });
        typewriterTimer.start();
    }

    private String toHtml(String text) {
        return "<html><body style='width:1120px'>" + text + "</body></html>";
    }
}

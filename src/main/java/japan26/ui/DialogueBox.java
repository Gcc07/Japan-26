package japan26.ui;

import japan26.model.Character;
import japan26.model.DialogueLine;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Consumer;

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

    private final PixelLabel nameLabel;
    private final PixelLabel textLabel;
    private final PixelLabel continueHint;
    private final JPanel     choicePanel;

    private Timer    typewriterTimer;
    private String   fullText  = "";
    private boolean  typing    = false;

    public DialogueBox() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 16, 30));

        nameLabel = new PixelLabel("");
        nameLabel.setFont(PixelFont.bold(20f));

        textLabel = new PixelLabel("");
        textLabel.setFont(PixelFont.regular(22f));
        textLabel.setForeground(Color.WHITE);

        continueHint = new PixelLabel("\u25BE");
        continueHint.setFont(PixelFont.bold(20f));
        continueHint.setHorizontalAlignment(SwingConstants.RIGHT);
        continueHint.setVisible(false);

        choicePanel = new JPanel();
        choicePanel.setOpaque(false);
        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
        choicePanel.setVisible(false);

        add(nameLabel);
        add(textLabel);
        add(choicePanel);
        add(continueHint);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void show(DialogueLine line, String displayText, Consumer<String> onChoiceSelected) {
        Character ch = line.getCharacter();

        boolean isNarrator = ch.getName().isEmpty();
        String speakerName = (ch == Character.PLAYER) ? SettingsState.getPlayerName() : ch.getName();
        nameLabel.setText(isNarrator ? "" : speakerName);
        nameLabel.setForeground(Color.decode(ch.getColor()));
        nameLabel.setVisible(!isNarrator);
        textLabel.setForeground(isNarrator ? new Color(235, 235, 235) : Color.WHITE);

        if (line.hasChoices()) {
            if (typewriterTimer != null) typewriterTimer.stop();
            typing = false;
            continueHint.setVisible(false);
            textLabel.setText(toHtml(displayText));
            renderChoices(line, onChoiceSelected);
            return;
        }

        choicePanel.setVisible(false);
        startTypewriter(displayText);
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

    private void renderChoices(DialogueLine line, Consumer<String> onChoiceSelected) {
        choicePanel.removeAll();
        for (String option : line.getChoiceOptions()) {
            PixelButton button = new PixelButton(option);
            button.setFont(PixelFont.bold(15f));
            button.setPlaySelectSound(false);
            button.setPreferredSize(new java.awt.Dimension(520, 36));
            button.setMaximumSize(new java.awt.Dimension(520, 36));
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.addActionListener(e -> {
                UISound.playSelect();
                onChoiceSelected.accept(option);
            });
            choicePanel.add(button);
            choicePanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 8)));
        }
        choicePanel.setVisible(true);
        revalidate();
        repaint();
    }

    private String toHtml(String text) {
        return "<html><body style='width:1120px'>" + text + "</body></html>";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(24, 20, 28, 200));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
        super.paintComponent(g);
    }
}

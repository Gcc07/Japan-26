package japan26.ui;

import japan26.engine.SceneManager;
import japan26.engine.StoryEngine;
import japan26.model.DialogueLine;
import japan26.model.StoryScene;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * Main game screen: a full-window StackPane with a background image layer and
 * a DialogueBox anchored to the bottom.
 *
 * Click anywhere (or press Space / Enter) to advance dialogue.
 */
public class GameView extends JPanel {

    private static final String FALLBACK_BG = "/japan26/images/Skyline.jpg";

    private final StoryEngine engine;
    private Image             background;
    private final DialogueBox dialogueBox;

    public GameView(StoryEngine engine, java.util.List<StoryScene> story) {
        this.engine = engine;

        setLayout(new BorderLayout());
        setFocusable(true);

        dialogueBox = new DialogueBox();
        dialogueBox.setPreferredSize(new java.awt.Dimension(1200, 180));

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 40, 30, 40));
        bottomBar.add(dialogueBox, BorderLayout.CENTER);

        add(bottomBar, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleAdvance(); }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "advance");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "advance");
        getActionMap().put("advance", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { handleAdvance(); }
        });

        // ── Wire story engine callbacks ────────────────────────────────────
        engine.setOnLineChanged(this::refreshLine);
        engine.setOnSceneChanged(this::refreshScene);
        engine.setOnStoryFinished(this::onStoryDone);

        engine.loadStory(story);
        requestFocusInWindow();
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
            if (url == null) {
                url = getClass().getResource(FALLBACK_BG);
            }
            if (url != null) {
                background = ImageIO.read(url);
            }
        } catch (IOException ignored) {
            // Background stays as-is if the image is missing
        }
        repaint();
    }

    private void onStoryDone() {
        SceneManager.showMainMenu();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

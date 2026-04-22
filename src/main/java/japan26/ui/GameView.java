package japan26.ui;

import japan26.engine.SceneManager;
import japan26.engine.StoryEngine;
import japan26.model.DialogueLine;
import japan26.model.StoryScene;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Main game screen: a full-window StackPane with a background image layer and
 * a DialogueBox anchored to the bottom.
 *
 * Click anywhere (or press Space / Enter) to advance dialogue.
 */
public class GameView extends JPanel {

    private static final String FALLBACK_BG = "/japan26/images/Skyline.jpg";
    private static final int PIXEL_SCALE = 4;

    private final StoryEngine engine;
    private BufferedImage     background;
    private BufferedImage     pixelBuffer;
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

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 14));
        topBar.setOpaque(false);
        PixelButton settingsBtn = new PixelButton("Settings");
        settingsBtn.setFont(PixelFont.bold(14f));
        settingsBtn.setPreferredSize(new java.awt.Dimension(170, 38));
        settingsBtn.setMaximumSize(new java.awt.Dimension(170, 38));
        settingsBtn.addActionListener(e -> SettingsDialog.show(SceneManager.getFrame(), false));
        topBar.add(settingsBtn);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        add(topBar, BorderLayout.NORTH);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleAdvance(); }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "advance");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "advance");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "settings");
        getActionMap().put("advance", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { handleAdvance(); }
        });
        getActionMap().put("settings", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                SettingsDialog.show(SceneManager.getFrame(), false);
            }
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
        pixelBuffer = null;
        repaint();
    }

    private void onStoryDone() {
        SceneManager.showMainMenu();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            Graphics2D g2 = (Graphics2D) g;
            int scaledW = Math.max(1, getWidth() / PIXEL_SCALE);
            int scaledH = Math.max(1, getHeight() / PIXEL_SCALE);

            if (pixelBuffer == null || pixelBuffer.getWidth() != scaledW || pixelBuffer.getHeight() != scaledH) {
                pixelBuffer = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D pg = pixelBuffer.createGraphics();
            pg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            pg.drawImage(background, 0, 0, scaledW, scaledH, null);
            pg.dispose();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(pixelBuffer, 0, 0, getWidth(), getHeight(), null);
        }
    }
}

package japan26.ui;

import japan26.engine.SceneManager;
import japan26.engine.StoryEngine;
import japan26.model.Character;
import japan26.model.DialogueLine;
import japan26.model.StoryScene;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
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
    private static final String BLACK_BG_TOKEN = "__BLACK__";
    private static final int PIXEL_SCALE = 4;
    private static final int ACTION_FADE_MS = 450;

    private final StoryEngine engine;
    private BufferedImage     background;
    private BufferedImage     pixelBuffer;
    private final DialogueBox             dialogueBox;
    private final DialogueCharacterStrip characterStrip;
    private final JPanel                  bottomBar;
    private final JPanel                  namePromptPanel;
    private       Character               conversationPartner = Character.NPC;
    private       boolean                 hasActivePartner    = false;
    /** Non-null only when story load is deferred until after name/outfit are confirmed. */
    private       Supplier<java.util.List<StoryScene>> deferredStorySupplier = null;
    private float                          actionFlashAlpha = 0f;
    private Timer                          actionFlashTimer;
    private boolean                        actionCueRunning = false;
    private boolean                        solidBlackBackground = false;

    public GameView(StoryEngine engine, java.util.List<StoryScene> story, boolean askNameAfterFade) {
        this(engine, story, askNameAfterFade, false);
    }

    public GameView(
            StoryEngine engine,
            java.util.List<StoryScene> story,
            boolean askNameAfterFade,
            boolean resumeExistingStoryState
    ) {
        this.engine = engine;

        setLayout(new BorderLayout());
        setFocusable(true);

        dialogueBox = new DialogueBox();
        dialogueBox.setPreferredSize(new java.awt.Dimension(1200, 180));

        characterStrip = new DialogueCharacterStrip();

        bottomBar = new JPanel(new BorderLayout(0, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 40, 30, 40));
        bottomBar.add(characterStrip, BorderLayout.NORTH);
        bottomBar.add(dialogueBox, BorderLayout.CENTER);
        bottomBar.setVisible(!askNameAfterFade);

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

        namePromptPanel = createNamePromptPanel();
        add(namePromptPanel, BorderLayout.CENTER);
        namePromptPanel.setVisible(askNameAfterFade);

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

        if (resumeExistingStoryState) {
            refreshScene();
        } else if (!askNameAfterFade) {
            engine.loadStory(story);
        }
        // When askNameAfterFade=true, story is loaded by deferredStorySupplier
        // inside the confirm handler after name/preset are written to SettingsState.
        requestFocusInWindow();
    }

    public void setDeferredStorySupplier(Supplier<java.util.List<StoryScene>> supplier) {
        this.deferredStorySupplier = supplier;
    }

    // ── Advance logic ─────────────────────────────────────────────────────────

    private void handleAdvance() {
        if (namePromptPanel.isVisible()) return;
        if (actionCueRunning) return;
        DialogueLine line = engine.getCurrentLine();
        if (line != null && line.hasChoices()) return;
        if (dialogueBox.isTyping()) {
            dialogueBox.skipTyping();
        } else {
            engine.advance();
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void refreshScene() {
        StoryScene scene = engine.getCurrentScene();
        conversationPartner = Character.NPC; // clear previous NPC when scene changes
        hasActivePartner    = false;
        setBackground(scene.getDefaultBackground());
        refreshLine();
    }

    private void refreshLine() {
        DialogueLine line = engine.getCurrentLine();
        if (line == null) return;
        if (isFadeToBlackCue(line)) {
            runFadeToBlackCue();
            return;
        }
        if (line.changesBackground()) {
            setBackground(line.getBackgroundPath());
        } else if (line.hasChoiceBgPaths()) {
            String selectedChoice = engine.getSelectedChoice(line.getChoiceId());
            String bgForChoice = line.getBgForChoice(selectedChoice);
            if (bgForChoice != null) setBackground(bgForChoice);
        }
        Character ch = line.getCharacter();
        if (!ch.getName().isEmpty() && ch != Character.PLAYER && !line.hasChoices()) {
            conversationPartner  = ch;
            hasActivePartner     = true;
        }
        characterStrip.sync(line, conversationPartner, hasActivePartner);
        dialogueBox.show(line, engine.getCurrentLineText(), engine::choose);
    }

    private boolean isFadeToBlackCue(DialogueLine line) {
        if (line == null) return false;
        String txt = line.getText();
        if (txt == null) return false;
        String normalized = txt.trim().toLowerCase();
        return normalized.contains("fade to black");
    }

    private void runFadeToBlackCue() {
        if (actionCueRunning) return;
        actionCueRunning = true;
        bottomBar.setVisible(false);
        if (actionFlashTimer != null) {
            actionFlashTimer.stop();
        }
        long startMs = System.currentTimeMillis();
        actionFlashTimer = new Timer(16, e -> {
            float t = Math.min(1f, (System.currentTimeMillis() - startMs) / (float) ACTION_FADE_MS);
            // Fast in/out peak for a "split-second" black flash.
            float tri = t <= 0.5f ? (t * 2f) : ((1f - t) * 2f);
            actionFlashAlpha = Math.max(0f, Math.min(0.96f, tri * 0.96f));
            repaint();
            if (t >= 1f) {
                actionFlashTimer.stop();
                actionFlashAlpha = 0f;
                actionCueRunning = false;
                bottomBar.setVisible(true);
                repaint();
                engine.advance();
            }
        });
        actionFlashTimer.start();
    }

    private void setBackground(String resourcePath) {
        if (BLACK_BG_TOKEN.equals(resourcePath)) {
            solidBlackBackground = true;
            background = null;
            pixelBuffer = null;
            repaint();
            return;
        }
        solidBlackBackground = false;
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

    private JPanel createNamePromptPanel() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 120));
        wrapper.setOpaque(false);

        final JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(new java.awt.Color(20, 16, 28, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(168, 138, 196), 2),
                BorderFactory.createEmptyBorder(18, 20, 16, 20)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        PixelLabel label = new PixelLabel("Enter your name:");
        label.setFont(PixelFont.bold(16f));
        label.setForeground(new java.awt.Color(236, 226, 204));
        label.setAlignmentX(LEFT_ALIGNMENT);

        JTextField nameField = new JTextField(SettingsState.getPlayerName());
        nameField.setMaximumSize(new java.awt.Dimension(280, 34));
        nameField.setPreferredSize(new java.awt.Dimension(280, 34));
        nameField.setFont(PixelFont.regular(14f));
        nameField.setForeground(new java.awt.Color(245, 245, 245));
        nameField.setBackground(new java.awt.Color(38, 30, 52));
        nameField.setCaretColor(new java.awt.Color(245, 245, 245));
        nameField.setBorder(BorderFactory.createLineBorder(new java.awt.Color(154, 126, 186), 2));
        nameField.setAlignmentX(LEFT_ALIGNMENT);

        PixelLabel lookLabel = new PixelLabel("Choose your character:");
        lookLabel.setFont(PixelFont.bold(15f));
        lookLabel.setForeground(new java.awt.Color(236, 226, 204));
        lookLabel.setAlignmentX(LEFT_ALIGNMENT);

        final int[] selectedPreset = new int[1];
        selectedPreset[0] = SettingsState.getPlayerPresetIndex();
        if (!SettingsState.isPlayerPresetUnlocked(selectedPreset[0])) {
            selectedPreset[0] = SettingsState.firstUnlockedPlayerPreset();
        }
        SettingsState.setPlayerPresetIndex(selectedPreset[0]);

        Map<Integer, BufferedImage> spriteCache = new HashMap<>();
        final int previewPx = 128;

        JPanel previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int id = selectedPreset[0];
                BufferedImage img = spriteCache.computeIfAbsent(id, k -> {
                    try {
                        String path = "/japan26/Sprites/player" + k + ".png";
                        var url = GameView.class.getResource(path);
                        if (url == null) {
                            return null;
                        }
                        return ImageIO.read(url);
                    } catch (IOException ex) {
                        return null;
                    }
                });
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new java.awt.Color(38, 30, 52));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new java.awt.Color(154, 126, 186));
                g2.drawRect(0, 0, w - 1, h - 1);
                if (img != null) {
                    double scale = Math.min((double) (w - 8) / img.getWidth(), (double) (h - 8) / img.getHeight());
                    int dw = (int) Math.round(img.getWidth() * scale);
                    int dh = (int) Math.round(img.getHeight() * scale);
                    int x = (w - dw) / 2;
                    int y = (h - dh) / 2;
                    g2.drawImage(img, x, y, dw, dh, null);
                }
                if (!SettingsState.isPlayerPresetUnlocked(id)) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
                    g2.setColor(new java.awt.Color(12, 8, 20));
                    g2.fillRect(0, 0, w, h);
                    g2.setComposite(AlphaComposite.SrcOver);
                    g2.setColor(new java.awt.Color(236, 200, 120));
                    g2.setFont(PixelFont.bold(14f));
                    String msg = "LOCKED";
                    int tw = g2.getFontMetrics().stringWidth(msg);
                    g2.drawString(msg, (w - tw) / 2, h / 2 + 5);
                }
                g2.dispose();
            }
        };
        previewPanel.setOpaque(true);
        previewPanel.setBackground(new java.awt.Color(38, 30, 52));
        previewPanel.setPreferredSize(new Dimension(previewPx, previewPx));
        previewPanel.setMaximumSize(new Dimension(previewPx, previewPx));
        previewPanel.setAlignmentX(LEFT_ALIGNMENT);

        PixelLabel lockHint = new PixelLabel("");
        lockHint.setFont(PixelFont.regular(12f));
        lockHint.setForeground(new java.awt.Color(200, 160, 120));
        lockHint.setAlignmentX(LEFT_ALIGNMENT);
        lockHint.setOpaque(true);
        lockHint.setBackground(new java.awt.Color(20, 16, 28));
        lockHint.setPreferredSize(new java.awt.Dimension(280, 18));
        lockHint.setMaximumSize(new java.awt.Dimension(280, 18));
        lockHint.setMinimumSize(new java.awt.Dimension(0, 18));

        PixelButton arrowLeft = new PixelButton("<");
        PixelButton arrowRight = new PixelButton(">");
        arrowLeft.setFont(PixelFont.bold(16f));
        arrowRight.setFont(PixelFont.bold(16f));
        arrowLeft.setPreferredSize(new Dimension(48, previewPx));
        arrowRight.setPreferredSize(new Dimension(48, previewPx));
        arrowLeft.setMaximumSize(new Dimension(48, previewPx));
        arrowRight.setMaximumSize(new Dimension(48, previewPx));

        PixelButton confirm = new PixelButton("Continue");
        confirm.setFont(PixelFont.bold(15f));
        confirm.setPreferredSize(new java.awt.Dimension(200, 38));
        confirm.setMaximumSize(new java.awt.Dimension(200, 38));
        confirm.setAlignmentX(LEFT_ALIGNMENT);
        confirm.addActionListener(e -> {
            if (!SettingsState.isPlayerPresetUnlocked(selectedPreset[0])) {
                return;
            }
            SettingsState.setPlayerName(nameField.getText());
            SettingsState.setPlayerPresetIndex(selectedPreset[0]);
            // Build and load the story NOW — after name/preset are set —
            // so branches that depend on them (e.g. McCuen+player11) evaluate correctly.
            if (deferredStorySupplier != null) {
                engine.loadStory(deferredStorySupplier.get());
                deferredStorySupplier = null;
            }
            namePromptPanel.setVisible(false);
            bottomBar.setVisible(true);
            refreshLine();
            revalidate();
            repaint();
            requestFocusInWindow();
        });

        JPanel charRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        charRow.setOpaque(false);
        charRow.setAlignmentX(LEFT_ALIGNMENT);
        charRow.add(arrowLeft);
        charRow.add(previewPanel);
        charRow.add(arrowRight);

        Runnable refreshCharacterRow = () -> {
            SettingsState.setPlayerPresetIndex(selectedPreset[0]);
            boolean unlocked = SettingsState.isPlayerPresetUnlocked(selectedPreset[0]);
            lockHint.setText(unlocked ? "" : "This look is locked for now.");
            confirm.setEnabled(unlocked);
            lockHint.revalidate();
            previewPanel.repaint();
            lockHint.repaint();
        };

        arrowLeft.addActionListener(e -> {
            selectedPreset[0]--;
            if (selectedPreset[0] < SettingsState.PLAYER_PRESET_MIN) {
                selectedPreset[0] = SettingsState.PLAYER_PRESET_MAX;
            }
            refreshCharacterRow.run();
        });
        arrowRight.addActionListener(e -> {
            selectedPreset[0]++;
            if (selectedPreset[0] > SettingsState.PLAYER_PRESET_MAX) {
                selectedPreset[0] = SettingsState.PLAYER_PRESET_MIN;
            }
            refreshCharacterRow.run();
        });

        refreshCharacterRow.run();

        card.add(label);
        card.add(Box.createRigidArea(new java.awt.Dimension(0, 8)));
        card.add(nameField);
        card.add(Box.createRigidArea(new java.awt.Dimension(0, 14)));
        card.add(lookLabel);
        card.add(Box.createRigidArea(new java.awt.Dimension(0, 8)));
        card.add(charRow);
        card.add(Box.createRigidArea(new java.awt.Dimension(0, 4)));
        card.add(lockHint);
        card.add(Box.createRigidArea(new java.awt.Dimension(0, 10)));
        card.add(confirm);
        wrapper.add(card);
        return wrapper;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (solidBlackBackground) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new java.awt.Color(0, 0, 0));
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (actionFlashAlpha > 0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, actionFlashAlpha));
                g2.setColor(new java.awt.Color(0, 0, 0));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
            return;
        }
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
            if (actionFlashAlpha > 0f) {
                Graphics2D overlay = (Graphics2D) g2.create();
                overlay.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, actionFlashAlpha));
                overlay.setColor(new java.awt.Color(0, 0, 0));
                overlay.fillRect(0, 0, getWidth(), getHeight());
                overlay.dispose();
            }
        }
    }
}

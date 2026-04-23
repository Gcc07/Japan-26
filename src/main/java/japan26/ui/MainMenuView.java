package japan26.ui;

import japan26.engine.SceneManager;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Title / main menu screen.
 */
public class MainMenuView extends JPanel {

    private static final int    PIXEL_SCALE  = 4;
    private static final float  FLOAT_AMP    = 6f;   // px up/down
    private static final float  FLOAT_PERIOD = 3800f; // ms per full cycle
    private static final int    BG_SWAP_MS   = 8000;

    private static final String[] MENU_BACKGROUNDS = new String[] {
            "/japan26/images/Skyline.jpg",
            "/japan26/images/OutsideCherry.jpg",
            "/japan26/images/Crossing 2.jpg",
            "/japan26/images/RainyDay.jpg",
            "/japan26/images/Skytree.jpg"
    };

    private BufferedImage background;
    private BufferedImage pixelBuffer;
    private int           backgroundIndex = 0;

    private final JLayeredPane           layered;
    private final CherryBlossomAnimation blossoms;
    private final JPanel                 content;
    private final JPanel                 creditsOverlay;
    private final PixelButton            creditsButton;
    private float                        floatOffset = 0f;

    public MainMenuView() {
        setLayout(new BorderLayout());

        layered = new JLayeredPane();
        layered.setLayout(null);
        layered.setOpaque(false);
        add(layered, BorderLayout.CENTER);

        blossoms = new CherryBlossomAnimation();
        blossoms.setEnabled(false);
        layered.add(blossoms, JLayeredPane.DEFAULT_LAYER);

        creditsButton = menuButton("Credits");
        content = buildContentPanel();
        layered.add(content, JLayeredPane.DEFAULT_LAYER);

        creditsOverlay = CreditsDialog.createCreditsOverlay(this::hideCreditsOverlay);
        creditsOverlay.setVisible(false);
        layered.add(creditsOverlay, JLayeredPane.MODAL_LAYER);

        creditsButton.addActionListener(e -> {
            creditsOverlay.setVisible(true);
            creditsOverlay.revalidate();
            repaint();
        });

        loadBackground(MENU_BACKGROUNDS[backgroundIndex]);

        Timer floatTimer = new Timer(16, e -> {
            float t = (System.currentTimeMillis() % (long) FLOAT_PERIOD) / FLOAT_PERIOD;
            floatOffset = (float) (FLOAT_AMP * Math.sin(2 * Math.PI * t));
            revalidate();
        });
        floatTimer.start();

        Timer bgTimer = new Timer(BG_SWAP_MS, e -> cycleBackground());
        bgTimer.start();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = layered.getWidth();
        int h = layered.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        blossoms.setBounds(0, 0, w, h);
        Dimension pref = content.getPreferredSize();
        int cx = (w - pref.width) / 2;
        int cy = (h - pref.height) / 2 + (int) floatOffset;
        content.setBounds(cx, cy, pref.width, pref.height);
        creditsOverlay.setBounds(0, 0, w, h);
    }

    private JPanel buildContentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = createTitleGraphic();

        PixelLabel subtitle = new PixelLabel("A totally normal trip to remember.");
        subtitle.setFont(PixelFont.regular(20f));
        subtitle.setForeground(new Color(230, 230, 230));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        PixelButton startBtn = menuButton("Begin Journey");
        startBtn.setPlaySelectSound(false);
        startBtn.addActionListener(e -> startGameWithStartSound());
        PixelButton testBtn = menuButton("Test Story");
        testBtn.setPlaySelectSound(false);
        testBtn.addActionListener(e -> startTestStoryWithStartSound());
        PixelButton minigameTestBtn = menuButton("Minigames Test");
        minigameTestBtn.setPlaySelectSound(false);
        minigameTestBtn.addActionListener(e -> startMinigameTestWithStartSound());
        PixelButton quitBtn = menuButton("Quit");
        quitBtn.addActionListener(e -> System.exit(0));
        PixelButton settingsBtn = menuButton("Settings");
        settingsBtn.addActionListener(e -> SettingsDialog.show(SceneManager.getFrame(), true));

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(startBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(testBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(minigameTestBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(settingsBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(creditsButton);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(quitBtn);

        SwingUtilities.invokeLater(startBtn::requestFocusInWindow);
        return panel;
    }

    private JLabel createTitleGraphic() {
        var titleUrl = getClass().getResource("/japan26/UI/Japan26.png");
        if (titleUrl != null) {
            JLabel label = new JLabel(new ImageIcon(titleUrl));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            return label;
        }
        PixelLabel fallback = new PixelLabel("JAPAN 26");
        fallback.setFont(PixelFont.bold(66f));
        fallback.setForeground(new Color(240, 205, 112));
        fallback.setAlignmentX(Component.CENTER_ALIGNMENT);
        return fallback;
    }

    private PixelButton menuButton(String text) {
        PixelButton button = new PixelButton(text);
        button.setPreferredSize(new Dimension(300, 44));
        button.setMaximumSize(new Dimension(300, 44));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(PixelFont.bold(18f));
        return button;
    }

    private void startGameWithStartSound() {
        UISound.playStart();
        SceneManager.startGameWithNamePrompt();
    }

    private void startTestStoryWithStartSound() {
        UISound.playStart();
        SceneManager.startTestStory();
    }

    private void startMinigameTestWithStartSound() {
        UISound.playStart();
        SceneManager.startMinigameTestStory();
    }

    private void hideCreditsOverlay() {
        creditsOverlay.setVisible(false);
        repaint();
    }

    private void cycleBackground() {
        backgroundIndex = (backgroundIndex + 1) % MENU_BACKGROUNDS.length;
        loadBackground(MENU_BACKGROUNDS[backgroundIndex]);
    }

    private void loadBackground(String resourcePath) {
        try {
            var bgUrl = getClass().getResource(resourcePath);
            if (bgUrl != null) {
                background = ImageIO.read(bgUrl);
                pixelBuffer = null;
                repaint();
            }
        } catch (IOException ignored) {
            // Keep previous background if this image fails to load
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (background != null) {
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
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(35, 35, 45), 0, getHeight(), new Color(10, 10, 16)));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}

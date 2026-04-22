package japan26.ui;

import japan26.engine.SceneManager;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
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

    private static final int PIXEL_SCALE = 4;

    private BufferedImage background;
    private BufferedImage pixelBuffer;

    private final CherryBlossomAnimation blossoms;
    private final JPanel content;

    public MainMenuView() {
        setLayout(null);

        blossoms = new CherryBlossomAnimation();
        blossoms.setEnabled(false);
        add(blossoms);

        content = buildContentPanel();
        add(content);

        try {
            var bgUrl = getClass().getResource("/japan26/images/OutsideCherry.jpg");
            if (bgUrl != null) background = ImageIO.read(bgUrl);
        } catch (IOException ignored) {
            // Keep null background if image cannot be read
        }
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        blossoms.setBounds(0, 0, w, h);
        Dimension pref = content.getPreferredSize();
        int cx = (w - pref.width) / 2;
        int cy = (h - pref.height) / 2;
        content.setBounds(cx, cy, pref.width, pref.height);
    }

    private JPanel buildContentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        PixelLabel title = new PixelLabel("JAPAN 26");
        title.setFont(PixelFont.bold(66f));
        title.setForeground(new Color(240, 205, 112));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        PixelLabel subtitle = new PixelLabel("A Story-Driven Journey  -  Summer 2026");
        subtitle.setFont(PixelFont.regular(20f));
        subtitle.setForeground(new Color(230, 230, 230));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        PixelButton startBtn = menuButton("Begin Journey");
        startBtn.addActionListener(e -> SceneManager.startGame());
        PixelButton testBtn = menuButton("Test Story");
        testBtn.addActionListener(e -> SceneManager.startTestStory());
        PixelButton quitBtn = menuButton("Quit");
        quitBtn.addActionListener(e -> System.exit(0));
        PixelButton settingsBtn = menuButton("Settings");
        settingsBtn.addActionListener(e -> SettingsDialog.show(SceneManager.getFrame(), true));
        PixelButton creditsBtn = menuButton("Credits");
        creditsBtn.addActionListener(e -> CreditsDialog.show(SceneManager.getFrame()));

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 40)));
        panel.add(startBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(testBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(quitBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(settingsBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(creditsBtn);

        SwingUtilities.invokeLater(startBtn::requestFocusInWindow);
        return panel;
    }

    private PixelButton menuButton(String text) {
        PixelButton button = new PixelButton(text);
        button.setPreferredSize(new Dimension(300, 44));
        button.setMaximumSize(new Dimension(300, 44));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(PixelFont.bold(18f));
        return button;
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

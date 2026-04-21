package japan26.ui;

import japan26.engine.SceneManager;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;

/**
 * Title / main menu screen.
 */
public class MainMenuView extends JPanel {

    private Image background;

    public MainMenuView() {
        setLayout(null);

        CherryBlossomAnimation blossoms = new CherryBlossomAnimation();
        blossoms.setBounds(0, 0, 1280, 720);
        blossoms.setEnabled(false);
        add(blossoms);

        JPanel content = buildContentPanel();
        content.setBounds(390, 120, 500, 480);
        add(content);

        try {
            var bgUrl = getClass().getResource("/japan26/images/OutsideCherry.jpg");
            if (bgUrl != null) background = ImageIO.read(bgUrl);
        } catch (IOException ignored) {
            // Keep null background if image cannot be read
        }
    }

    private JPanel buildContentPanel() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("JAPAN 26");
        title.setFont(new Font("Segoe UI", Font.BOLD, 72));
        title.setForeground(new Color(240, 205, 112));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("A Story-Driven Journey  -  Summer 2026");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        subtitle.setForeground(new Color(230, 230, 230));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startBtn = menuButton("Begin Journey");
        startBtn.addActionListener(e -> SceneManager.startGame());
        JButton testBtn = menuButton("Test Story");
        testBtn.addActionListener(e -> SceneManager.startTestStory());
        JButton quitBtn = menuButton("Quit");
        quitBtn.addActionListener(e -> System.exit(0));

        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 16)));
        content.add(subtitle);
        content.add(Box.createRigidArea(new Dimension(0, 40)));
        content.add(startBtn);
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(testBtn);
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(quitBtn);
        return content;
    }

    private JButton menuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(300, 44));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (background != null) {
            g2.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(35, 35, 45), 0, getHeight(), new Color(10, 10, 16)));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}

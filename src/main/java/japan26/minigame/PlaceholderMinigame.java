package japan26.minigame;

import japan26.ui.PixelButton;
import japan26.ui.PixelFont;
import japan26.ui.PixelLabel;
import japan26.ui.UISound;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Simple placeholder minigame used for story/minigame flow testing.
 */
public class PlaceholderMinigame implements Minigame {
    private final String title;
    private final String instructions;
    private Runnable onComplete;

    public PlaceholderMinigame(String title, String instructions) {
        this.title = title;
        this.instructions = instructions;
    }

    @Override
    public void start(JFrame frame) {
        JPanel root = new JPanel();
        root.setBackground(new Color(16, 14, 24));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(110, 90, 90, 90));

        PixelLabel titleLabel = new PixelLabel(title);
        titleLabel.setFont(PixelFont.bold(34f));
        titleLabel.setForeground(new Color(242, 210, 131));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        PixelLabel info = new PixelLabel("<html><div style='text-align:center;width:740px'>" + instructions + "</div></html>");
        info.setFont(PixelFont.regular(17f));
        info.setForeground(new Color(226, 226, 226));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        PixelButton complete = new PixelButton("Complete Minigame");
        complete.setFont(PixelFont.bold(18f));
        complete.setPreferredSize(new Dimension(320, 44));
        complete.setMaximumSize(new Dimension(320, 44));
        complete.setAlignmentX(Component.CENTER_ALIGNMENT);
        complete.addActionListener(e -> {
            UISound.playSelect();
            if (onComplete != null) onComplete.run();
        });

        root.add(titleLabel);
        root.add(Box.createRigidArea(new Dimension(0, 18)));
        root.add(info);
        root.add(Box.createRigidArea(new Dimension(0, 26)));
        root.add(complete);

        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
    }

    @Override
    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }
}

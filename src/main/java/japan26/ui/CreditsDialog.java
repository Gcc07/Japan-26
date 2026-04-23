package japan26.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * Credits content shown in-app (e.g. as an overlay on the main menu).
 */
public final class CreditsDialog {
    private CreditsDialog() {
    }

    /**
     * Full-size semi-transparent overlay with a centered credits card.
     * {@code onClose} runs when the player chooses Back.
     */
    public static JPanel createCreditsOverlay(Runnable onClose) {
        JPanel dim = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(8, 6, 12, 228));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        dim.setOpaque(false);

        JPanel card = buildCreditsCard(onClose);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        dim.add(card, c);
        return dim;
    }

    private static JPanel buildCreditsCard(Runnable onClose) {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        root.setBackground(new Color(18, 14, 26));

        root.add(heading("CREDITS"));
        root.add(Box.createRigidArea(new Dimension(0, 22)));

        root.add(divider());
        root.add(Box.createRigidArea(new Dimension(0, 14)));

        root.add(role("Art, Pictures & Game Direction"));
        root.add(Box.createRigidArea(new Dimension(0, 6)));
        root.add(name("Gabe Cardenas"));
        root.add(Box.createRigidArea(new Dimension(0, 18)));

        root.add(divider());
        root.add(Box.createRigidArea(new Dimension(0, 14)));

        root.add(role("Game Design & Story"));
        root.add(Box.createRigidArea(new Dimension(0, 6)));
        root.add(name("Gabe Cardenas"));
        root.add(Box.createRigidArea(new Dimension(0, 18)));

        root.add(divider());
        root.add(Box.createRigidArea(new Dimension(0, 14)));

        root.add(role("Programming Assistance"));
        root.add(Box.createRigidArea(new Dimension(0, 6)));
        root.add(name("Cursor AI"));
        root.add(Box.createRigidArea(new Dimension(0, 18)));

        root.add(divider());
        root.add(Box.createRigidArea(new Dimension(0, 14)));

        root.add(role("UI Sound Effects"));
        root.add(Box.createRigidArea(new Dimension(0, 6)));
        root.add(name("JDSherbert"));
        root.add(Box.createRigidArea(new Dimension(0, 22)));

        root.add(divider());
        root.add(Box.createRigidArea(new Dimension(0, 22)));

        PixelButton backBtn = new PixelButton("Back");
        backBtn.setFont(PixelFont.bold(16f));
        backBtn.setPreferredSize(new Dimension(260, 40));
        backBtn.setMaximumSize(new Dimension(260, 40));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> onClose.run());
        root.add(backBtn);

        SwingUtilities.invokeLater(backBtn::requestFocusInWindow);
        return root;
    }

    private static PixelLabel heading(String text) {
        PixelLabel l = new PixelLabel(text);
        l.setFont(PixelFont.bold(26f));
        l.setForeground(new Color(245, 219, 137));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private static PixelLabel role(String text) {
        PixelLabel l = new PixelLabel(text);
        l.setFont(PixelFont.regular(13f));
        l.setForeground(new Color(160, 148, 180));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private static PixelLabel name(String text) {
        PixelLabel l = new PixelLabel(text);
        l.setFont(PixelFont.bold(18f));
        l.setForeground(new Color(230, 230, 230));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(380, 1));
        sep.setForeground(new Color(80, 60, 100));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        return sep;
    }
}

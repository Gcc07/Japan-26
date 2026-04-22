package japan26.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

/**
 * Credits dialog shown from the main menu.
 */
public final class CreditsDialog {
    private CreditsDialog() {
    }

    public static void show(Frame owner) {
        JDialog dialog = new JDialog(owner, "Credits", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(owner);
        dialog.setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        root.setBackground(new Color(18, 14, 26));

        root.add(heading("CREDITS"));
        root.add(Box.createRigidArea(new Dimension(0, 22)));

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

        PixelButton closeBtn = new PixelButton("Close");
        closeBtn.setFont(PixelFont.bold(16f));
        closeBtn.setPreferredSize(new Dimension(260, 40));
        closeBtn.setMaximumSize(new Dimension(260, 40));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());
        root.add(closeBtn);

        dialog.setContentPane(root);
        dialog.setVisible(true);
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

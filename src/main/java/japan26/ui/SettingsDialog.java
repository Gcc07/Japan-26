package japan26.ui;

import japan26.engine.SceneManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

/**
 * Reusable settings dialog for menu and in-game screens.
 */
public final class SettingsDialog {
    private SettingsDialog() {
    }

    public static void show(Frame owner, boolean inMenu) {
        JDialog dialog = new JDialog(owner, "Settings", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(owner);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.setBackground(new Color(25, 20, 32));

        PixelLabel title = new PixelLabel("SETTINGS");
        title.setFont(PixelFont.bold(26f));
        title.setForeground(new Color(245, 219, 137));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        PixelLabel volumeLabel = new PixelLabel("Sound Volume");
        volumeLabel.setFont(PixelFont.regular(16f));
        volumeLabel.setForeground(new Color(230, 230, 230));
        volumeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSlider slider = new JSlider(0, 100, SettingsState.getVolumePercent());
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setOpaque(false);
        slider.setAlignmentX(Component.CENTER_ALIGNMENT);
        slider.addChangeListener(e -> SettingsState.setVolumePercent(slider.getValue()));

        JLabel pct = new PixelLabel(SettingsState.getVolumePercent() + "%");
        pct.setFont(PixelFont.regular(14f));
        pct.setForeground(new Color(210, 210, 210));
        pct.setAlignmentX(Component.CENTER_ALIGNMENT);
        slider.addChangeListener(e -> pct.setText(slider.getValue() + "%"));

        JCheckBox typewriterToggle = new JCheckBox("Typewriter SFX");
        typewriterToggle.setOpaque(false);
        typewriterToggle.setSelected(SettingsState.isTypewriterSfxEnabled());
        typewriterToggle.setFont(PixelFont.regular(15f));
        typewriterToggle.setForeground(new Color(230, 230, 230));
        typewriterToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        typewriterToggle.addActionListener(e ->
                SettingsState.setTypewriterSfxEnabled(typewriterToggle.isSelected()));

        PixelButton closeBtn = new PixelButton("Close");
        closeBtn.setFont(PixelFont.bold(16f));
        closeBtn.setPreferredSize(new Dimension(260, 40));
        closeBtn.setMaximumSize(new Dimension(260, 40));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());

        root.add(title);
        root.add(Box.createRigidArea(new Dimension(0, 16)));
        root.add(volumeLabel);
        root.add(Box.createRigidArea(new Dimension(0, 8)));
        root.add(slider);
        root.add(Box.createRigidArea(new Dimension(0, 4)));
        root.add(pct);
        root.add(Box.createRigidArea(new Dimension(0, 10)));
        root.add(typewriterToggle);
        root.add(Box.createRigidArea(new Dimension(0, 16)));

        if (!inMenu) {
            PixelButton menuBtn = new PixelButton("Return to Menu");
            menuBtn.setFont(PixelFont.bold(16f));
            menuBtn.setPreferredSize(new Dimension(260, 40));
            menuBtn.setMaximumSize(new Dimension(260, 40));
            menuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuBtn.addActionListener(e -> {
                dialog.dispose();
                SceneManager.showMainMenu();
            });
            root.add(menuBtn);
            root.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        PixelButton exitBtn = new PixelButton("Exit Game");
        exitBtn.setFont(PixelFont.bold(16f));
        exitBtn.setPreferredSize(new Dimension(260, 40));
        exitBtn.setMaximumSize(new Dimension(260, 40));
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.addActionListener(e -> System.exit(0));

        root.add(exitBtn);
        root.add(Box.createRigidArea(new Dimension(0, 10)));
        root.add(closeBtn);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}

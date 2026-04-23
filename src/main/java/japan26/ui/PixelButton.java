package japan26.ui;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Pixel-styled button with hover animation: grows slightly on hover.
 * Growth is achieved by shrinking insets at rest rather than scaling
 * the graphics transform, which avoids Swing's component-clip boundary.
 */
public class PixelButton extends JButton {
    private static final float HOVER_SPEED = 0.16f;
    private static final int   MAX_INSET   = 4; // px inset at rest, 0 at hover

    private final Timer animator;
    private float hoverProgress = 0f;
    private float targetProgress = 0f;
    private boolean playSelectSound = true;

    public PixelButton(String text) {
        super(text);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(new Color(244, 236, 214));
        setBackground(new Color(70, 52, 86));
        setFocusable(true);

        animator = new Timer(16, e -> animateStep());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                targetProgress = 1f;
                UISound.playHover();
                animator.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                targetProgress = 0f;
                animator.start();
            }
        });

        // Mouse-only hover visuals. Keyboard focus should not trigger hover styling.
        setFocusable(false);
        addActionListener(e -> {
            if (playSelectSound) UISound.playSelect();
        });
    }

    private void animateStep() {
        float diff = targetProgress - hoverProgress;
        if (Math.abs(diff) < 0.01f) {
            hoverProgress = targetProgress;
            animator.stop();
        } else {
            hoverProgress += diff * HOVER_SPEED;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        int w = getWidth();
        int h = getHeight();

        // At rest the button is inset by MAX_INSET; on hover the inset shrinks
        // to 0 so the fill expands to the full component bounds — no transform
        // scaling needed, so Swing's clip boundary is never exceeded.
        int press = getModel().isPressed() ? 2 : 0;
        float inset = MAX_INSET * (1f - hoverProgress) + press;

        Color fill   = blend(new Color(70, 52, 86), new Color(112, 80, 140), hoverProgress);
        Color border = blend(new Color(164, 138, 196), new Color(255, 214, 120), hoverProgress);
        if (!isEnabled()) {
            fill   = blend(fill, new Color(28, 24, 36), 0.55f);
            border = blend(border, new Color(80, 72, 96), 0.55f);
        }
        RoundRectangle2D shape = new RoundRectangle2D.Float(
                inset, inset, w - inset * 2, h - inset * 2, 12, 12);
        g2.setColor(fill);
        g2.fill(shape);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(shape);

        g2.dispose();
        super.paintComponent(g);
    }

    public void setPlaySelectSound(boolean playSelectSound) {
        this.playSelectSound = playSelectSound;
    }

    private static Color blend(Color from, Color to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * t);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * t);
        return new Color(r, g, b);
    }
}

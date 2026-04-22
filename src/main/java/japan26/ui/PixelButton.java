package japan26.ui;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pixel-styled button with hover animation: grows slightly on hover.
 */
public class PixelButton extends JButton {
    private static final float HOVER_SPEED  = 0.16f;
    private static final float MAX_SCALE    = 0.06f; // 6% larger on hover
    private static long lastHoverBeepMs = 0L;
    private static final ExecutorService SOUND_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ui-sound");
        t.setDaemon(true);
        return t;
    });

    private final Timer animator;
    private float hoverProgress = 0f;
    private float targetProgress = 0f;

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
                playHoverSound();
                animator.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                targetProgress = 0f;
                animator.start();
            }
        });

        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
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

        // Scale up from centre on hover, shrink slightly on press
        float scale = 1f + MAX_SCALE * hoverProgress - (getModel().isPressed() ? 0.02f : 0f);
        g2.translate(w / 2.0, h / 2.0);
        g2.scale(scale, scale);
        g2.translate(-w / 2.0, -h / 2.0);

        Color fill   = blend(new Color(70, 52, 86), new Color(112, 80, 140), hoverProgress);
        Color border = blend(new Color(164, 138, 196), new Color(255, 214, 120), hoverProgress);
        boolean focused = isFocusOwner();

        RoundRectangle2D shape = new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 12, 12);
        g2.setColor(fill);
        g2.fill(shape);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(shape);

        if (focused) {
            g2.setColor(new Color(255, 238, 167, 190));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 14, 14));
        }

        g2.dispose();
        super.paintComponent(g);
    }

    private void playHoverSound() {
        if (SettingsState.isMuted()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastHoverBeepMs < 90) {
            return;
        }
        lastHoverBeepMs = now;
        SOUND_EXECUTOR.submit(() -> Toolkit.getDefaultToolkit().beep());
    }

    private static Color blend(Color from, Color to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * t);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * t);
        return new Color(r, g, b);
    }
}

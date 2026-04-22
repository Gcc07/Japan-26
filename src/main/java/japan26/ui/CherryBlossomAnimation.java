package japan26.ui;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

/**
 * A JPanel that renders continuously falling cherry blossom petals.
 */
public class CherryBlossomAnimation extends JPanel {

    private static final int    COUNT = 60;
    private static final double W     = 1280;
    private static final double H     = 720;

    // Per-petal state in flat arrays (faster than object allocation per frame)
    private final double[] px, py, size, speed, angle, spin, swayAmp, swayPhase;
    private final Color[]  color;

    private final Timer          timer;
    private final Random         rng = new Random(42);
    private long                 lastNanos = System.nanoTime();

    public CherryBlossomAnimation() {
        setOpaque(false);

        px        = new double[COUNT];
        py        = new double[COUNT];
        size      = new double[COUNT];
        speed     = new double[COUNT];
        angle     = new double[COUNT];
        spin      = new double[COUNT];
        swayAmp   = new double[COUNT];
        swayPhase = new double[COUNT];
        color     = new Color[COUNT];

        for (int i = 0; i < COUNT; i++) spawnPetal(i, true);

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.05);
            lastNanos = now;
            update(dt);
            repaint();
        });
        timer.start();
    }

    // ── Petal initialisation ──────────────────────────────────────────────────

    private void spawnPetal(int i, boolean randomY) {
        px[i]       = rng.nextDouble() * W;
        py[i]       = randomY ? rng.nextDouble() * H : -(size[i] + 10);
        size[i]     = 7 + rng.nextDouble() * 10;
        speed[i]    = 38 + rng.nextDouble() * 55;
        angle[i]    = rng.nextDouble() * 360;
        spin[i]     = (rng.nextDouble() - 0.5) * 80;
        swayAmp[i]  = 18 + rng.nextDouble() * 35;
        swayPhase[i]= rng.nextDouble() * Math.PI * 2;

        double alpha = 0.50 + rng.nextDouble() * 0.45;
        color[i] = colorForPetal(alpha, rng.nextInt(4));
    }

    // ── Per-frame update ──────────────────────────────────────────────────────

    private void update(double dt) {
        double t = System.currentTimeMillis() / 1000.0;
        for (int i = 0; i < COUNT; i++) {
            py[i] += speed[i] * dt;
            px[i] += swayAmp[i] * Math.sin(t * 0.65 + swayPhase[i]) * dt;
            angle[i] += spin[i] * dt;
            if (py[i] > H + 20) spawnPetal(i, false);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < COUNT; i++) {
            Graphics2D p = (Graphics2D) g2.create();
            p.translate(px[i], py[i]);
            p.rotate(Math.toRadians(angle[i]));
            p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, color[i].getAlpha() / 255f));
            p.setColor(color[i]);

            double s = size[i];
            p.fillOval((int) (-s * 0.5), (int) (-s * 0.26), (int) s, (int) (s * 0.52));
            p.setColor(new Color(255, 245, 250, 130));
            p.fillOval((int) (-s * 0.18), (int) (-s * 0.26), (int) (s * 0.36), (int) (s * 0.22));
            p.dispose();
        }
    }

    private Color colorForPetal(double alpha, int variant) {
        int a = (int) (alpha * 255);
        return switch (variant) {
            case 0 -> new Color(255, 184, 196, a);
            case 1 -> new Color(255, 209, 219, a);
            case 2 -> new Color(255, 232, 237, a);
            default -> new Color(255, 248, 250, a);
        };
    }
}

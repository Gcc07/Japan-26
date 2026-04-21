package japan26.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * A Canvas that renders continuously falling cherry blossom petals.
 * Automatically starts/stops with the JavaFX scene lifecycle.
 * Mouse-transparent so clicks pass through to buttons underneath.
 */
public class CherryBlossomAnimation extends Canvas {

    private static final int    COUNT = 60;
    private static final double W     = 1280;
    private static final double H     = 720;

    // Per-petal state in flat arrays (faster than object allocation per frame)
    private final double[] px, py, size, speed, angle, spin, swayAmp, swayPhase;
    private final Color[]  color;

    private final AnimationTimer timer;
    private final Random         rng = new Random(42);
    private long lastNano = 0;

    public CherryBlossomAnimation() {
        super(W, H);
        setMouseTransparent(true);

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

        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                double dt = lastNano == 0 ? 0.016 : (now - lastNano) / 1_000_000_000.0;
                lastNano  = now;
                dt = Math.min(dt, 0.05);
                update(dt);
                render();
            }
        };

        // Tie the animation to the scene so it stops when the screen is swapped out
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) { lastNano = 0; timer.start(); }
            else                    timer.stop();
        });
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
        color[i] = switch (rng.nextInt(4)) {
            case 0  -> Color.color(1.00, 0.72, 0.77, alpha); // soft pink
            case 1  -> Color.color(1.00, 0.82, 0.86, alpha); // light pink
            case 2  -> Color.color(1.00, 0.91, 0.93, alpha); // pale pink
            default -> Color.color(1.00, 0.97, 0.98, alpha); // near-white
        };
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

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void render() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);

        for (int i = 0; i < COUNT; i++) {
            gc.save();
            gc.translate(px[i], py[i]);
            gc.rotate(angle[i]);
            gc.setFill(color[i]);

            double s = size[i];
            // Primary petal lobe
            gc.fillOval(-s * 0.5, -s * 0.26, s, s * 0.52);
            // Small notch hint at the tip (second smaller oval, slightly offset)
            gc.setFill(color[i].deriveColor(0, 1, 0.88, 0.5));
            gc.fillOval(-s * 0.18, -s * 0.26, s * 0.36, s * 0.22);

            gc.restore();
        }
    }
}

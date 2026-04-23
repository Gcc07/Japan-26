package japan26.minigame;

import japan26.ui.PixelButton;
import japan26.ui.PixelFont;
import japan26.ui.PixelLabel;
import japan26.ui.UISound;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 30-second rhythm test minigame.
 * Hit notes near the judgement line with A/S/K/L.
 */
public class RhythmDrumMinigame implements Minigame {
    private static final int DURATION_MS = 30_000;
    private static final int TARGET_SCORE = 280;
    private static final String REWARD_ITEM = "phil collins drumsticks";
    private static final int BPM = 95;
    private static final int BEAT_INTERVAL_MS = (int) Math.round(60_000.0 / BPM);
    private static final Path TRACK_PATH = Paths.get(
            "src", "main", "java", "japan26", "SFX", "SFX pack", "i-can-feel-it-coming-in-the-air-tonight.wav");
    private static final Path TEMPLATE_PATH = Paths.get(
            "src", "main", "resources", "japan26", "minigame", "rhythm_template.csv");

    private Runnable onComplete;

    @Override
    public void start(JFrame frame) {
        RhythmPanel panel = new RhythmPanel(() -> {
            if (onComplete != null) onComplete.run();
        });
        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
        panel.requestFocusForInput();
    }

    @Override
    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }

    private static class RhythmPanel extends JPanel {
        private static final float NOTE_SPEED_PX_PER_MS = 0.36f;
        private static final int NOTE_SPAWN_Y = -26;
        /** Top edge of the judgment bar (thick hit line). */
        private static final int JUDGMENT_Y = 350;
        private static final int JUDGMENT_LINE_THICKNESS = 4;
        private static final int NOTE_WIDTH = 56;
        private static final int NOTE_HEIGHT = 18;
        /**
         * Note {@code y} is the top of the bar. When the note center meets the judgment line center,
         * the top sits here — used for travel time, hit windows, and recording/playback alignment.
         */
        private static final int NOTE_HIT_TOP_Y =
                JUDGMENT_Y + JUDGMENT_LINE_THICKNESS / 2 - NOTE_HEIGHT / 2;
        private static final float JUDGMENT_LINE_CENTER_Y =
                JUDGMENT_Y + JUDGMENT_LINE_THICKNESS / 2f;
        private static final int NOTE_TRAVEL_MS =
                Math.round((NOTE_HIT_TOP_Y - NOTE_SPAWN_Y) / NOTE_SPEED_PX_PER_MS);

        private final Random rng = new Random();
        private final List<Note> notes = new ArrayList<>();
        private final LaneCanvas laneCanvas = new LaneCanvas();

        private final PixelLabel timeLabel = new PixelLabel("Time: 30.0");
        private final PixelLabel scoreLabel = new PixelLabel("Score: 0");
        private final PixelLabel infoLabel = new PixelLabel("Keys: A / S / K / L");
        private final PixelButton finishButton = new PixelButton("Finish");
        private final PixelButton recordButton = new PixelButton("Record Template");
        private final PixelButton clearTemplateButton = new PixelButton("Clear Template");

        private final Runnable onFinish;
        private Timer gameLoop;
        private long lastMs;
        private int remainingMs = DURATION_MS;
        private int score = 0;
        private int combo = 0;
        private boolean finished = false;
        private Clip musicClip;
        private boolean recordingMode = false;
        private final List<TemplateNote> recordedTemplate = new ArrayList<>();
        private final List<TemplateNote> templateNotes = new ArrayList<>();
        private int templateSpawnIndex = 0;
        /** Hidden test controls unlock after typing G then C. */
        private boolean debugControlsUnlocked = false;
        private int debugUnlockProgress = 0;

        private int spawnAccumulatorMs = 0;

        /** Per-lane key-press highlight, 0–1 (decays each frame). */
        private final float[] laneGlow = new float[4];
        private final List<HitParticle> hitParticles = new ArrayList<>();
        /** Full-canvas red miss / wrong-press overlay strength, 0–1. */
        private float redFlash = 0f;

        RhythmPanel(Runnable onFinishCallback) {
            this.onFinish = onFinishCallback;
            setLayout(new BorderLayout());
            setBackground(new Color(14, 12, 22));

            JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 26, 10));
            top.setOpaque(false);
            stylizeLabel(timeLabel, 16f, new Color(242, 226, 188));
            stylizeLabel(scoreLabel, 16f, new Color(188, 224, 255));
            stylizeLabel(infoLabel, 14f, new Color(214, 214, 214));
            infoLabel.setText("95 BPM  |  Keys: A / S / K / L");
            loadTemplate();
            top.add(timeLabel);
            top.add(scoreLabel);
            top.add(infoLabel);
            recordButton.setFont(PixelFont.bold(14f));
            recordButton.setPreferredSize(new Dimension(220, 34));
            recordButton.setMaximumSize(new Dimension(220, 34));
            recordButton.addActionListener(e -> beginTemplateRecording());
            recordButton.setVisible(false);
            top.add(recordButton);

            clearTemplateButton.setFont(PixelFont.bold(14f));
            clearTemplateButton.setPreferredSize(new Dimension(220, 34));
            clearTemplateButton.setMaximumSize(new Dimension(220, 34));
            clearTemplateButton.addActionListener(e -> clearTemplate());
            clearTemplateButton.setVisible(false);
            top.add(clearTemplateButton);

            JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
            centerWrap.setOpaque(false);
            laneCanvas.setPreferredSize(new Dimension(760, 420));
            centerWrap.add(laneCanvas);

            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
            bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

            finishButton.setFont(PixelFont.bold(16f));
            finishButton.setPreferredSize(new Dimension(240, 40));
            finishButton.setMaximumSize(new Dimension(240, 40));
            finishButton.setAlignmentX(CENTER_ALIGNMENT);
            finishButton.setVisible(false);
            finishButton.addActionListener(e -> this.onFinish.run());

            bottom.add(Box.createRigidArea(new Dimension(0, 8)));
            bottom.add(finishButton);

            add(top, BorderLayout.NORTH);
            add(centerWrap, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            bindKey("A", 0);
            bindKey("S", 1);
            bindKey("K", 2);
            bindKey("L", 3);
            bindKey("SPACE", -1); // optional alternative to clear nearest lane note
            bindHiddenKey("G", 'G');
            bindHiddenKey("C", 'C');

            startBackingTrack();
            startLoop();
        }

        void requestFocusForInput() {
            requestFocusInWindow();
        }

        private void stylizeLabel(PixelLabel label, float size, Color color) {
            label.setFont(PixelFont.bold(size));
            label.setForeground(color);
        }

        private void startLoop() {
            lastMs = System.currentTimeMillis();
            gameLoop = new Timer(16, e -> {
                long now = System.currentTimeMillis();
                int dt = (int) Math.min(40, now - lastMs);
                lastMs = now;
                tick(dt);
            });
            gameLoop.start();
        }

        private void tick(int dtMs) {
            if (finished) return;

            remainingMs = Math.max(0, remainingMs - dtMs);
            int elapsedMs = DURATION_MS - remainingMs;
            if (!recordingMode) {
                if (!templateNotes.isEmpty()) {
                    while (templateSpawnIndex < templateNotes.size()
                            && templateNotes.get(templateSpawnIndex).spawnTimeMs() <= elapsedMs) {
                        TemplateNote t = templateNotes.get(templateSpawnIndex++);
                        notes.add(new Note(t.lane, NOTE_SPAWN_Y));
                    }
                } else {
                    spawnAccumulatorMs += dtMs;
                    while (spawnAccumulatorMs >= BEAT_INTERVAL_MS) {
                        spawnAccumulatorMs -= BEAT_INTERVAL_MS;
                        // Keep rhythm steady while allowing slight variation in density.
                        if (rng.nextFloat() < 0.82f) {
                            notes.add(new Note(rng.nextInt(4), NOTE_SPAWN_Y));
                        }
                    }
                }
            }

            Iterator<Note> it = notes.iterator();
            while (it.hasNext()) {
                Note n = it.next();
                n.y += dtMs * NOTE_SPEED_PX_PER_MS;
                if (n.y + NOTE_HEIGHT / 2f > JUDGMENT_LINE_CENTER_Y + 52f) {
                    it.remove();
                    combo = 0;
                    if (!recordingMode) {
                        triggerRedFlash();
                    }
                }
            }

            float glowDecay = (float) Math.pow(0.88, dtMs / 16.0);
            for (int i = 0; i < laneGlow.length; i++) {
                laneGlow[i] *= glowDecay;
            }
            Iterator<HitParticle> pit = hitParticles.iterator();
            while (pit.hasNext()) {
                HitParticle p = pit.next();
                p.tick(dtMs);
                if (p.dead()) {
                    pit.remove();
                }
            }
            if (redFlash > 0) {
                redFlash -= dtMs * 0.0055f;
                if (redFlash < 0) {
                    redFlash = 0;
                }
            }

            timeLabel.setText(String.format("Time: %.1f", remainingMs / 1000f));
            scoreLabel.setText("Score: " + score);
            laneCanvas.repaint();

            if (remainingMs <= 0) {
                finishRound();
            }
        }

        private void bindKey(String key, int lane) {
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), "hit_" + key);
            getActionMap().put("hit_" + key, new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (finished) return;
                    handleHit(lane);
                }
            });
        }

        private void bindHiddenKey(String key, char c) {
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), "hidden_" + key);
            getActionMap().put("hidden_" + key, new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (finished) return;
                    handleHiddenUnlock(c);
                }
            });
        }

        private void handleHiddenUnlock(char key) {
            if (debugControlsUnlocked) return;
            if (debugUnlockProgress == 0) {
                debugUnlockProgress = (key == 'G') ? 1 : 0;
                return;
            }
            if (debugUnlockProgress == 1 && key == 'C') {
                debugControlsUnlocked = true;
                recordButton.setVisible(true);
                clearTemplateButton.setVisible(true);
                infoLabel.setText("Hax enabled: template tools unlocked.");
                infoLabel.setForeground(new Color(255, 223, 158));
                revalidate();
                repaint();
                return;
            }
            debugUnlockProgress = (key == 'G') ? 1 : 0;
        }

        private void pulseLane(int laneIdx) {
            if (laneIdx >= 0 && laneIdx < laneGlow.length) {
                laneGlow[laneIdx] = 1f;
            }
        }

        private void triggerRedFlash() {
            redFlash = Math.min(0.85f, Math.max(redFlash, 0.62f));
        }

        private void spawnHitDust(int laneIdx) {
            int cw = laneCanvas.getWidth();
            if (cw < 40) {
                return;
            }
            int laneW = cw / 4;
            float cx = laneIdx * laneW + laneW / 2f;
            for (int i = 0; i < 20; i++) {
                hitParticles.add(new HitParticle(
                        cx + (rng.nextFloat() - 0.5f) * 48f,
                        JUDGMENT_LINE_CENTER_Y + (rng.nextFloat() - 0.5f) * 10f,
                        rng));
            }
        }

        private void handleHit(int lane) {
            if (recordingMode && lane >= 0) {
                pulseLane(lane);
                int elapsedMs = DURATION_MS - remainingMs;
                recordedTemplate.add(new TemplateNote(elapsedMs, lane));
                UISound.playSelect();
                return;
            }

            if (lane >= 0) {
                pulseLane(lane);
            }

            Note best = null;
            float bestDistance = Float.MAX_VALUE;

            for (Note n : notes) {
                if (lane >= 0 && n.lane != lane) continue;
                float noteCenterY = n.y + NOTE_HEIGHT / 2f;
                float d = Math.abs(noteCenterY - JUDGMENT_LINE_CENTER_Y);
                if (d < bestDistance) {
                    bestDistance = d;
                    best = n;
                }
            }

            if (best == null || bestDistance > 42f) {
                combo = 0;
                if (!recordingMode) {
                    triggerRedFlash();
                }
                return;
            }

            if (lane < 0) {
                pulseLane(best.lane);
            }

            notes.remove(best);
            spawnHitDust(best.lane);
            if (bestDistance <= 16f) {
                score += 22;
                combo++;
            } else if (bestDistance <= 28f) {
                score += 14;
                combo++;
            } else {
                score += 8;
                combo = 0;
            }
            if (combo > 0 && combo % 10 == 0) {
                score += 20; // combo bonus
            }
            UISound.playSelect();
        }

        private void finishRound() {
            finished = true;
            gameLoop.stop();
            notes.clear();
            hitParticles.clear();
            for (int i = 0; i < laneGlow.length; i++) {
                laneGlow[i] = 0f;
            }
            redFlash = 0f;
            stopBackingTrack();
            recordButton.setVisible(false);

            if (recordingMode) {
                saveTemplate(recordedTemplate);
                infoLabel.setText("Template saved (" + recordedTemplate.size() + " notes).");
                infoLabel.setForeground(new Color(168, 243, 168));
            } else {
                boolean won = score >= TARGET_SCORE;
                if (won) {
                    PlayerRewards.unlock(REWARD_ITEM);
                    infoLabel.setText("Cleared! Reward unlocked: " + REWARD_ITEM);
                    infoLabel.setForeground(new Color(168, 243, 168));
                } else {
                    infoLabel.setText("Score " + score + "/" + TARGET_SCORE + " - no reward this run.");
                    infoLabel.setForeground(new Color(255, 188, 188));
                }
            }

            finishButton.setVisible(true);
            revalidate();
            repaint();
        }

        private void startBackingTrack() {
            try (AudioInputStream stream = AudioSystem.getAudioInputStream(TRACK_PATH.toFile())) {
                musicClip = AudioSystem.getClip();
                musicClip.open(stream);
                musicClip.start();
            } catch (Exception ignored) {
                // mp3 support depends on local JVM codecs; game still plays without track.
                infoLabel.setText("95 BPM  |  (Track unavailable on this JVM)  |  Keys: A / S / K / L");
            }
        }

        private void stopBackingTrack() {
            if (musicClip == null) return;
            try {
                musicClip.stop();
                musicClip.close();
            } catch (Exception ignored) {
                // No-op
            }
        }

        private void beginTemplateRecording() {
            recordingMode = true;
            recordedTemplate.clear();
            notes.clear();
            hitParticles.clear();
            for (int i = 0; i < laneGlow.length; i++) {
                laneGlow[i] = 0f;
            }
            redFlash = 0f;
            score = 0;
            combo = 0;
            remainingMs = DURATION_MS;
            templateSpawnIndex = 0;
            spawnAccumulatorMs = 0;
            infoLabel.setText("Recording template... play A/S/K/L to capture note timing.");
            infoLabel.setForeground(new Color(255, 223, 158));
            timeLabel.setText("Time: 30.0");
            scoreLabel.setText("Score: REC");
            stopBackingTrack();
            startBackingTrack();
        }

        private void loadTemplate() {
            templateNotes.clear();
            if (!Files.exists(TEMPLATE_PATH)) return;
            try {
                List<String> lines = Files.readAllLines(TEMPLATE_PATH, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] parts = line.split(",");
                    if (parts.length != 2) continue;
                    int timeMs = Integer.parseInt(parts[0].trim());
                    int lane = Integer.parseInt(parts[1].trim());
                    if (lane >= 0 && lane <= 3 && timeMs >= 0 && timeMs <= DURATION_MS) {
                        templateNotes.add(new TemplateNote(timeMs, lane));
                    }
                }
                templateNotes.sort((a, b) -> Integer.compare(a.timeMs, b.timeMs));
                if (!templateNotes.isEmpty()) {
                    infoLabel.setText("95 BPM  |  Template loaded (" + templateNotes.size() + " notes)");
                }
            } catch (NumberFormatException | IOException ignored) {
                // Fall back to generated notes if template parse fails.
                templateNotes.clear();
            }
        }

        private void saveTemplate(List<TemplateNote> template) {
            try {
                Files.createDirectories(TEMPLATE_PATH.getParent());
                List<String> lines = new ArrayList<>();
                lines.add("# timeMs,lane — timeMs = elapsed when note center should cross judgment line (lane: 0–3)");
                for (TemplateNote n : template) {
                    lines.add(n.timeMs + "," + n.lane);
                }
                Files.write(TEMPLATE_PATH, lines, StandardCharsets.UTF_8);
                templateNotes.clear();
                templateNotes.addAll(template);
                templateNotes.sort((a, b) -> Integer.compare(a.timeMs, b.timeMs));
            } catch (IOException ignored) {
                infoLabel.setText("Template save failed (check file permissions).");
                infoLabel.setForeground(new Color(255, 188, 188));
            }
        }

        private void clearTemplate() {
            try {
                Files.deleteIfExists(TEMPLATE_PATH);
            } catch (IOException ignored) {
                // Continue with in-memory clear even if file deletion fails.
            }
            templateNotes.clear();
            templateSpawnIndex = 0;
            infoLabel.setText("Template cleared. Using generated 95 BPM notes.");
            infoLabel.setForeground(new Color(255, 223, 158));
        }

        private class LaneCanvas extends JPanel {
            private static final int LANE_COUNT = 4;

            LaneCanvas() {
                setOpaque(true);
                setBackground(new Color(14, 11, 24));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(120, 92, 168), 2),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                int w = getWidth();
                int h = getHeight();
                int laneW = w / LANE_COUNT;

                var topGlow = new RadialGradientPaint(
                        w / 2f, 0f, w * 0.55f,
                        new float[] { 0f, 0.45f, 1f },
                        new Color[] {
                                new Color(88, 64, 140, 90),
                                new Color(40, 32, 72, 40),
                                new Color(22, 18, 38, 0)
                        },
                        MultipleGradientPaint.CycleMethod.NO_CYCLE);
                g2.setPaint(topGlow);
                g2.fillRect(0, 0, w, h);

                Color laneDeep = new Color(26, 20, 44);
                Color laneAlt  = new Color(20, 16, 38);
                for (int i = 0; i < LANE_COUNT; i++) {
                    int x = i * laneW;
                    var laneGrad = new LinearGradientPaint(
                            x, 0f, x + laneW, h,
                            new float[] { 0f, 0.5f, 1f },
                            new Color[] {
                                    i % 2 == 0 ? laneDeep : laneAlt,
                                    i % 2 == 0 ? new Color(34, 28, 56) : new Color(28, 22, 48),
                                    i % 2 == 0 ? laneAlt : laneDeep
                            },
                            MultipleGradientPaint.CycleMethod.NO_CYCLE);
                    g2.setPaint(laneGrad);
                    g2.fillRect(x, 0, laneW, h);
                    g2.setColor(new Color(255, 255, 255, 28));
                    g2.drawLine(x + laneW - 1, 0, x + laneW - 1, h);
                    g2.setColor(new Color(100, 78, 150, 110));
                    g2.drawLine(x, 0, x, h);
                }

                for (int i = 0; i < LANE_COUNT; i++) {
                    float glow = laneGlow[i];
                    if (glow <= 0.02f) continue;
                    int x = i * laneW;
                    g2.setColor(new Color(255, 245, 200, (int) (95 * glow)));
                    g2.fillRect(x, 0, laneW, h);
                    var pressGrad = new RadialGradientPaint(
                            x + laneW / 2f, JUDGMENT_LINE_CENTER_Y, laneW * 0.55f,
                            new float[] { 0f, 0.55f, 1f },
                            new Color[] {
                                    new Color(255, 255, 255, (int) (120 * glow)),
                                    new Color(180, 230, 255, (int) (70 * glow)),
                                    new Color(120, 180, 255, 0)
                            },
                            MultipleGradientPaint.CycleMethod.NO_CYCLE);
                    g2.setPaint(pressGrad);
                    g2.fillRect(x, 0, laneW, h);
                }

                int jy = JUDGMENT_Y;
                int jt = JUDGMENT_LINE_THICKNESS;
                for (int pass = 3; pass >= 1; pass--) {
                    float a = 0.12f * pass;
                    g2.setColor(new Color(255, 200, 120, (int) (255 * a)));
                    g2.fillRoundRect(-2, jy - pass * 2, w + 4, jt + pass * 4, 8, 8);
                }
                var lineGrad = new LinearGradientPaint(
                        0f, jy, 0f, jy + jt,
                        new float[] { 0f, 0.5f, 1f },
                        new Color[] {
                                new Color(255, 236, 160),
                                new Color(255, 255, 220),
                                new Color(255, 190, 100)
                        },
                        MultipleGradientPaint.CycleMethod.NO_CYCLE);
                g2.setPaint(lineGrad);
                g2.fillRoundRect(0, jy, w, jt, 4, 4);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRect(0, jy + jt / 2 - 1, w, 2);

                for (Note n : notes) {
                    int x = n.lane * laneW + laneW / 2 - NOTE_WIDTH / 2;
                    int y = (int) n.y;
                    g2.setColor(new Color(0, 0, 0, 70));
                    g2.fillRoundRect(x + 2, y + 3, NOTE_WIDTH, NOTE_HEIGHT, 10, 10);
                    var noteGrad = new LinearGradientPaint(
                            x, y, x, y + NOTE_HEIGHT,
                            new float[] { 0f, 0.45f, 1f },
                            new Color[] {
                                    new Color(200, 248, 255),
                                    new Color(120, 200, 255),
                                    new Color(70, 140, 220)
                            },
                            MultipleGradientPaint.CycleMethod.NO_CYCLE);
                    g2.setPaint(noteGrad);
                    g2.fill(new RoundRectangle2D.Float(x, y, NOTE_WIDTH, NOTE_HEIGHT, 10, 10));
                    g2.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, 200),
                            x + NOTE_WIDTH, y + NOTE_HEIGHT, new Color(255, 255, 255, 0)));
                    g2.fill(new RoundRectangle2D.Float(x + 4, y + 2, NOTE_WIDTH / 3f, NOTE_HEIGHT / 2.5f, 6, 6));
                    g2.setColor(new Color(230, 250, 255));
                    g2.draw(new RoundRectangle2D.Float(x, y, NOTE_WIDTH, NOTE_HEIGHT, 10, 10));
                    g2.setColor(new Color(40, 90, 140, 180));
                    g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, NOTE_WIDTH - 1, NOTE_HEIGHT - 1, 9, 9));
                }

                Composite prev = g2.getComposite();
                for (HitParticle p : hitParticles) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha()));
                    g2.setColor(p.color);
                    float s = p.size;
                    g2.fill(new Ellipse2D.Float(p.x - s / 2, p.y - s / 2, s, s));
                    g2.setColor(new Color(255, 255, 255, 220));
                    g2.fill(new Ellipse2D.Float(p.x - s / 4, p.y - s / 4, s / 2, s / 2));
                }
                g2.setComposite(prev);

                if (redFlash > 0.01f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, redFlash * 0.55f));
                    g2.setColor(new Color(220, 40, 55));
                    g2.fillRect(0, 0, w, h);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, redFlash * 0.22f));
                    g2.setColor(new Color(255, 200, 200));
                    g2.fillRect(0, 0, w, h);
                    g2.setComposite(prev);
                }

                g2.setFont(PixelFont.bold(13f));
                String[] keys = { "A", "S", "K", "L" };
                for (int i = 0; i < LANE_COUNT; i++) {
                    int cx = i * laneW + laneW / 2;
                    int pillW = 36;
                    int pillH = 26;
                    int px = cx - pillW / 2;
                    int py = h - pillH - 10;
                    var pillGrad = new LinearGradientPaint(
                            px, py, px, py + pillH,
                            new float[] { 0f, 1f },
                            new Color[] { new Color(52, 42, 78), new Color(34, 28, 58) },
                            MultipleGradientPaint.CycleMethod.NO_CYCLE);
                    g2.setPaint(pillGrad);
                    g2.fillRoundRect(px, py, pillW, pillH, 10, 10);
                    g2.setColor(new Color(160, 130, 210));
                    g2.drawRoundRect(px, py, pillW, pillH, 10, 10);
                    g2.setColor(new Color(220, 210, 245));
                    int sw = g2.getFontMetrics().stringWidth(keys[i]);
                    g2.drawString(keys[i], cx - sw / 2, py + 18);
                }
                g2.dispose();
            }
        }
    }

    private static class Note {
        int lane;
        float y;

        Note(int lane, float y) {
            this.lane = lane;
            this.y = y;
        }
    }

    /** Small debris drifting downward from a successful hit. */
    private static final class HitParticle {
        float x;
        float y;
        float vx;
        float vy;
        float life;
        final float maxLife;
        final float size;
        final Color color;

        HitParticle(float x, float y, Random rng) {
            this.x = x;
            this.y = y;
            this.vx = (rng.nextFloat() - 0.5f) * 0.42f;
            this.vy = 0.22f + rng.nextFloat() * 0.38f;
            this.maxLife = 320f + rng.nextFloat() * 280f;
            this.life = maxLife;
            this.size = 2.2f + rng.nextFloat() * 5f;
            int r = 220 + rng.nextInt(35);
            int g = 200 + rng.nextInt(50);
            int b = 160 + rng.nextInt(60);
            this.color = new Color(
                    Math.min(255, r),
                    Math.min(255, g),
                    Math.min(255, b));
        }

        void tick(int dtMs) {
            life -= dtMs;
            x += vx * dtMs;
            y += vy * dtMs;
            vy += 0.00035f * dtMs;
            vx *= (float) Math.pow(0.997, dtMs);
        }

        boolean dead() {
            return life <= 0;
        }

        float alpha() {
            return Math.min(1f, life / Math.min(140f, maxLife * 0.35f));
        }
    }

    private static class TemplateNote {
        final int timeMs;
        final int lane;

        TemplateNote(int timeMs, int lane) {
            this.timeMs = timeMs;
            this.lane = lane;
        }

        int spawnTimeMs() {
            return Math.max(0, timeMs - RhythmPanel.NOTE_TRAVEL_MS);
        }
    }
}

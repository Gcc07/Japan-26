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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
        private final Random rng = new Random();
        private final List<Note> notes = new ArrayList<>();
        private final LaneCanvas laneCanvas = new LaneCanvas();

        private final PixelLabel timeLabel = new PixelLabel("Time: 30.0");
        private final PixelLabel scoreLabel = new PixelLabel("Score: 0");
        private final PixelLabel infoLabel = new PixelLabel("Keys: A / S / K / L");
        private final PixelButton finishButton = new PixelButton("Finish");

        private final Runnable onFinish;
        private Timer gameLoop;
        private long lastMs;
        private int remainingMs = DURATION_MS;
        private int score = 0;
        private int combo = 0;
        private boolean finished = false;
        private Clip musicClip;

        private int spawnAccumulatorMs = 0;

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
            top.add(timeLabel);
            top.add(scoreLabel);
            top.add(infoLabel);

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
            spawnAccumulatorMs += dtMs;
            while (spawnAccumulatorMs >= BEAT_INTERVAL_MS) {
                spawnAccumulatorMs -= BEAT_INTERVAL_MS;
                // Keep rhythm steady while allowing slight variation in density.
                if (rng.nextFloat() < 0.82f) {
                    notes.add(new Note(rng.nextInt(4), -26f));
                }
            }

            Iterator<Note> it = notes.iterator();
            while (it.hasNext()) {
                Note n = it.next();
                n.y += dtMs * 0.36f;
                if (n.y > LaneCanvas.JUDGMENT_Y + 80) {
                    it.remove();
                    combo = 0;
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

        private void handleHit(int lane) {
            Note best = null;
            float bestDistance = Float.MAX_VALUE;

            for (Note n : notes) {
                if (lane >= 0 && n.lane != lane) continue;
                float d = Math.abs(n.y - LaneCanvas.JUDGMENT_Y);
                if (d < bestDistance) {
                    bestDistance = d;
                    best = n;
                }
            }

            if (best == null || bestDistance > 42f) {
                combo = 0;
                return;
            }

            notes.remove(best);
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
            stopBackingTrack();

            boolean won = score >= TARGET_SCORE;
            if (won) {
                PlayerRewards.unlock(REWARD_ITEM);
                infoLabel.setText("Cleared! Reward unlocked: " + REWARD_ITEM);
                infoLabel.setForeground(new Color(168, 243, 168));
            } else {
                infoLabel.setText("Score " + score + "/" + TARGET_SCORE + " - no reward this run.");
                infoLabel.setForeground(new Color(255, 188, 188));
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

        private class LaneCanvas extends JPanel {
            private static final int LANE_COUNT = 4;
            private static final int JUDGMENT_Y = 350;

            LaneCanvas() {
                setOpaque(true);
                setBackground(new Color(22, 18, 34));
                setBorder(BorderFactory.createLineBorder(new Color(98, 78, 122), 2));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int laneW = w / LANE_COUNT;

                for (int i = 0; i < LANE_COUNT; i++) {
                    int x = i * laneW;
                    g2.setColor(i % 2 == 0 ? new Color(38, 30, 58) : new Color(32, 26, 50));
                    g2.fillRect(x, 0, laneW, h);
                    g2.setColor(new Color(80, 68, 104));
                    g2.drawLine(x, 0, x, h);
                }
                g2.setColor(new Color(255, 219, 130));
                g2.fillRect(0, JUDGMENT_Y, w, 4);

                for (Note n : notes) {
                    int x = n.lane * laneW + laneW / 2 - 28;
                    int y = (int) n.y;
                    g2.setColor(new Color(156, 220, 255));
                    g2.fillRoundRect(x, y, 56, 18, 10, 10);
                    g2.setColor(new Color(220, 245, 255));
                    g2.drawRoundRect(x, y, 56, 18, 10, 10);
                }

                g2.setColor(new Color(196, 196, 224));
                g2.setFont(PixelFont.regular(14f));
                g2.drawString("A", laneW / 2 - 4, h - 12);
                g2.drawString("S", laneW + laneW / 2 - 4, h - 12);
                g2.drawString("K", laneW * 2 + laneW / 2 - 4, h - 12);
                g2.drawString("L", laneW * 3 + laneW / 2 - 4, h - 12);
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
}

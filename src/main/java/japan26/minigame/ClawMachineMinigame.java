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
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

/**
 * Simple arcade-style claw machine minigame.
 * Move left/right and press Space to drop the claw.
 */
public class ClawMachineMinigame implements Minigame {
    private static final String REWARD_ITEM = "arcade plush";
    private Runnable onComplete;

    @Override
    public void start(JFrame frame) {
        ClawPanel panel = new ClawPanel(() -> {
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

    private static final class ClawPanel extends JPanel {
        private static final int LANES = 6;
        private static final int START_TIME_MS = 22_000;
        private static final int MAX_DROPS = 4;

        private final PixelLabel infoLabel = new PixelLabel("Move: A/D or arrows  |  Drop: SPACE");
        private final PixelLabel scoreLabel = new PixelLabel("Caught: 0");
        private final PixelLabel timeLabel = new PixelLabel("Time: 22.0");
        private final PixelButton finishButton = new PixelButton("Continue");
        private final MachineCanvas canvas = new MachineCanvas();

        private final Runnable onDone;
        private final Random rng = new Random();
        private final boolean[] hasPrize = new boolean[LANES];

        private Timer timer;
        private long lastMs;
        private int remainingMs = START_TIME_MS;
        private int clawLane = LANES / 2;
        private int catches = 0;
        private int dropsLeft = MAX_DROPS;
        private float clawY = 30f;
        private int conveyorAccumulator = 0;
        private boolean roundOver = false;

        private enum ClawState { IDLE, DROPPING, RAISING }
        private ClawState clawState = ClawState.IDLE;

        ClawPanel(Runnable onDone) {
            this.onDone = onDone;
            setLayout(new BorderLayout());
            setBackground(new Color(16, 14, 24));

            JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 26, 10));
            top.setOpaque(false);
            styleLabel(infoLabel, 14f, new Color(230, 220, 200));
            styleLabel(scoreLabel, 16f, new Color(180, 235, 255));
            styleLabel(timeLabel, 16f, new Color(245, 216, 152));
            top.add(infoLabel);
            top.add(scoreLabel);
            top.add(timeLabel);

            JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
            centerWrap.setOpaque(false);
            canvas.setPreferredSize(new Dimension(860, 460));
            centerWrap.add(canvas);

            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
            bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
            finishButton.setFont(PixelFont.bold(16f));
            finishButton.setPreferredSize(new Dimension(240, 40));
            finishButton.setMaximumSize(new Dimension(240, 40));
            finishButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            finishButton.setVisible(false);
            finishButton.addActionListener(e -> this.onDone.run());
            bottom.add(Box.createRigidArea(new Dimension(0, 8)));
            bottom.add(finishButton);

            add(top, BorderLayout.NORTH);
            add(centerWrap, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            seedPrizes();
            bindKeys();
            startLoop();
        }

        void requestFocusForInput() {
            requestFocusInWindow();
        }

        private void styleLabel(PixelLabel label, float size, Color color) {
            label.setFont(PixelFont.bold(size));
            label.setForeground(color);
        }

        private void seedPrizes() {
            for (int i = 0; i < LANES; i++) {
                hasPrize[i] = rng.nextFloat() < 0.70f;
            }
            // Ensure at least one possible target.
            boolean any = false;
            for (boolean v : hasPrize) any |= v;
            if (!any) hasPrize[rng.nextInt(LANES)] = true;
        }

        private void bindKeys() {
            bindAction("A", "left", () -> moveClaw(-1));
            bindAction("LEFT", "left_arrow", () -> moveClaw(-1));
            bindAction("D", "right", () -> moveClaw(1));
            bindAction("RIGHT", "right_arrow", () -> moveClaw(1));
            bindAction("SPACE", "drop", this::dropClaw);
        }

        private void bindAction(String key, String actionId, Runnable action) {
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), actionId);
            getActionMap().put(actionId, new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (roundOver) return;
                    action.run();
                }
            });
        }

        private void moveClaw(int delta) {
            if (clawState != ClawState.IDLE) return;
            clawLane = Math.max(0, Math.min(LANES - 1, clawLane + delta));
            UISound.playHover();
            canvas.repaint();
        }

        private void dropClaw() {
            if (clawState != ClawState.IDLE || dropsLeft <= 0) return;
            clawState = ClawState.DROPPING;
            dropsLeft--;
            UISound.playSelect();
            infoLabel.setText("Drops left: " + dropsLeft);
        }

        private void startLoop() {
            lastMs = System.currentTimeMillis();
            timer = new Timer(16, e -> {
                long now = System.currentTimeMillis();
                int dt = (int) Math.min(40, now - lastMs);
                lastMs = now;
                tick(dt);
            });
            timer.start();
        }

        private void tick(int dt) {
            if (roundOver) return;
            remainingMs = Math.max(0, remainingMs - dt);
            timeLabel.setText(String.format("Time: %.1f", remainingMs / 1000f));

            int bottomY = canvas.getPrizeY() - 40;
            if (clawState == ClawState.DROPPING) {
                clawY += dt * 0.52f;
                if (clawY >= bottomY) {
                    clawY = bottomY;
                    if (hasPrize[clawLane]) {
                        hasPrize[clawLane] = false;
                        catches++;
                        scoreLabel.setText("Caught: " + catches);
                        UISound.playStart();
                    }
                    clawState = ClawState.RAISING;
                }
            } else if (clawState == ClawState.RAISING) {
                clawY -= dt * 0.56f;
                if (clawY <= 30f) {
                    clawY = 30f;
                    clawState = ClawState.IDLE;
                }
            }

            // Conveyor drift every ~2.1s while idle to keep it alive.
            if (clawState == ClawState.IDLE) {
                conveyorAccumulator += dt;
                if (conveyorAccumulator >= 2100) {
                    conveyorAccumulator = 0;
                    shiftPrizes(rng.nextBoolean() ? 1 : -1);
                }
            }

            canvas.repaint();
            if (remainingMs <= 0 || dropsLeft <= 0) {
                finishRound();
            }
        }

        private void shiftPrizes(int dir) {
            boolean edge = dir > 0 ? hasPrize[LANES - 1] : hasPrize[0];
            if (dir > 0) {
                System.arraycopy(hasPrize, 0, hasPrize, 1, LANES - 1);
                hasPrize[0] = edge;
            } else {
                System.arraycopy(hasPrize, 1, hasPrize, 0, LANES - 1);
                hasPrize[LANES - 1] = edge;
            }
        }

        private void finishRound() {
            roundOver = true;
            timer.stop();
            if (catches >= 2) {
                PlayerRewards.unlock(REWARD_ITEM);
                infoLabel.setText("Nice! You won a plush.");
                infoLabel.setForeground(new Color(168, 243, 168));
            } else {
                infoLabel.setText("No big win this time. Caught: " + catches);
                infoLabel.setForeground(new Color(255, 198, 188));
            }
            finishButton.setVisible(true);
            revalidate();
            repaint();
        }

        private final class MachineCanvas extends JPanel {
            MachineCanvas() {
                setOpaque(true);
                setBackground(new Color(26, 22, 38));
                setBorder(BorderFactory.createLineBorder(new Color(123, 96, 174), 2));
            }

            int getPrizeY() {
                return getHeight() - 85;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int laneW = w / LANES;

                g2.setColor(new Color(40, 32, 64));
                g2.fillRoundRect(14, 14, w - 28, h - 28, 16, 16);
                g2.setColor(new Color(74, 62, 108));
                g2.drawRoundRect(14, 14, w - 28, h - 28, 16, 16);

                // Lane separators
                g2.setColor(new Color(96, 78, 140, 90));
                for (int i = 1; i < LANES; i++) {
                    int x = i * laneW;
                    g2.drawLine(x, 0, x, h);
                }

                // Prize shelf.
                int prizeY = getPrizeY();
                g2.setColor(new Color(86, 68, 120));
                g2.fillRoundRect(20, prizeY + 22, w - 40, 18, 10, 10);

                // Prizes.
                for (int i = 0; i < LANES; i++) {
                    if (!hasPrize[i]) continue;
                    int cx = i * laneW + laneW / 2;
                    g2.setColor(new Color(230, 196, 126));
                    g2.fillOval(cx - 24, prizeY - 10, 48, 38);
                    g2.setColor(new Color(255, 236, 190));
                    g2.fillOval(cx - 8, prizeY - 2, 16, 12);
                    g2.setColor(new Color(132, 96, 62));
                    g2.drawOval(cx - 24, prizeY - 10, 48, 38);
                }

                // Rail and claw.
                int clawX = clawLane * laneW + laneW / 2;
                g2.setColor(new Color(152, 146, 182));
                g2.fillRect(20, 18, w - 40, 10);
                g2.setColor(new Color(214, 214, 240));
                g2.drawLine(clawX, 24, clawX, (int) clawY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(clawX - 14, (int) clawY - 6, 14, 24, 250, 110);
                g2.drawArc(clawX, (int) clawY - 6, 14, 24, 180, 110);

                // Crosshair at active lane.
                g2.setColor(new Color(255, 228, 156, 170));
                g2.drawLine(clawX - 18, 48, clawX + 18, 48);
                g2.drawLine(clawX, 36, clawX, 60);

                g2.dispose();
            }
        }
    }
}

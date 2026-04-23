package japan26.ui;

import japan26.model.Character;
import japan26.model.DialogueLine;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Two portrait slots (you on the left, partner / speaker on the right) above the
 * dialogue box. The active speaker scales up; idle speaker is greyed out slightly.
 * Transitions lerp smoothly in both directions.
 */
public class DialogueCharacterStrip extends JPanel {

    /** Scale of the portrait while speaking (1.0 = base). */
    private static final float ACTIVE_SCALE  = 1.14f;
    /** Scale of the portrait while idle. */
    private static final float IDLE_SCALE    = 1.0f;
    /** Alpha composite brightness reduction for the idle / background character. */
    private static final float IDLE_ALPHA    = 0.52f;
    /** Lerp factor per 16 ms frame (higher = snappier). 0.18 ≈ ~0.1 s to mostly settle. */
    private static final float LERP_SPEED    = 0.18f;

    private static final int   STRIP_H  = 300;
    private static final int   PLAYER_W = 460;
    /** Extra horizontal offset for the right (NPC) portrait. */
    private static final int   RIGHT_SLOT_SHIFT_X = 40;

    /** Shared NPC dialogue art (mirrored on the right vs player on the left). */
    private static final String NPC_SPRITE_PATH = "/japan26/Sprites/question.png";
    private static BufferedImage cachedNpcSprite;

    private final PortraitSlot leftSlot;
    private final PortraitSlot rightSlot;
    private final Timer        lerpTimer;

    public DialogueCharacterStrip() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 40));
        setLayout(new BorderLayout(16, 0));

        leftSlot  = new PortraitSlot(true);
        rightSlot = new PortraitSlot(false);
        add(leftSlot,  BorderLayout.WEST);
        add(rightSlot, BorderLayout.EAST);
        // Same width so both portraits use the same fit scale (narrow right slot made NPC art tiny).
        leftSlot.setPreferredSize(new Dimension(PLAYER_W, STRIP_H));
        rightSlot.setPreferredSize(new Dimension(PLAYER_W, STRIP_H));
        leftSlot.setMinimumSize(new Dimension(PLAYER_W / 2, STRIP_H));
        rightSlot.setMinimumSize(new Dimension(PLAYER_W / 2, STRIP_H));

        lerpTimer = new Timer(16, e -> {
            boolean needsRepaint = false;
            needsRepaint |= leftSlot.stepLerp();
            needsRepaint |= rightSlot.stepLerp();
            if (needsRepaint) {
                leftSlot.repaint();
                rightSlot.repaint();
            }
        });
        lerpTimer.start();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        int w = d.width > 0 ? d.width : 800;
        return new Dimension(w, STRIP_H);
    }

    public void sync(DialogueLine line, Character partner) {
        Character ch           = line.getCharacter();
        boolean   choiceLine   = line.hasChoices();
        boolean   narrator     = ch.getName().isEmpty();
        boolean   playerActive = ch == Character.PLAYER && !choiceLine;

        leftSlot.setPortrait(resolvePlayerPortrait());
        leftSlot.setPlaceholderTint(decodeColor(Character.PLAYER.getColor()));

        Character rightSubject = partner;
        if (!playerActive && !narrator && !choiceLine) {
            rightSubject = ch;
        }
        rightSlot.setPortrait(resolvePortrait(rightSubject));
        rightSlot.setPlaceholderTint(decodeColor(rightSubject.getColor()));

        boolean inConversation = !narrator && !choiceLine;
        rightSlot.setVisible(inConversation);

        boolean leftActive  = inConversation && playerActive;
        boolean rightActive = inConversation && !playerActive;
        leftSlot.setActive(leftActive);
        rightSlot.setActive(rightActive);
        leftSlot.repaint();
        rightSlot.repaint();
        revalidate();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Color decodeColor(String hex) {
        try {
            return Color.decode(hex.startsWith("#") ? hex : "#" + hex);
        } catch (IllegalArgumentException e) {
            return new Color(130, 180, 220);
        }
    }

    private static BufferedImage resolvePlayerPortrait() {
        return loadImage(SettingsState.getPlayerSpriteResourcePath());
    }

    private static BufferedImage resolvePortrait(Character who) {
        if (who == Character.PLAYER) {
            return resolvePlayerPortrait();
        }
        if (cachedNpcSprite == null) {
            cachedNpcSprite = loadImage(NPC_SPRITE_PATH);
        }
        return cachedNpcSprite;
    }

    private static BufferedImage loadImage(String resourcePath) {
        try {
            var url = DialogueCharacterStrip.class.getResource(resourcePath);
            if (url == null) return null;
            return ImageIO.read(url);
        } catch (IOException ignored) {
            return null;
        }
    }

    // ── Portrait slot ─────────────────────────────────────────────────────────

    private static final class PortraitSlot extends JPanel {

        private final boolean   playerSide;
        private BufferedImage   portrait;
        private Color           placeholderTint = new Color(120, 110, 140);

        /** true = this character is currently speaking */
        private boolean active = false;
        /** Current interpolated scale (lerps toward target). */
        private float   currentScale = IDLE_SCALE;
        /** Current interpolated alpha. */
        private float   currentAlpha = IDLE_ALPHA;

        PortraitSlot(boolean playerSide) {
            this.playerSide = playerSide;
            setOpaque(false);
        }

        void setPortrait(BufferedImage img)  { this.portrait = img; }

        void setPlaceholderTint(Color c) {
            if (c != null) placeholderTint = c;
        }

        void setActive(boolean active) { this.active = active; }

        /** Called each timer tick. Lerps scale/alpha and returns true if values changed. */
        boolean stepLerp() {
            float targetScale = active ? ACTIVE_SCALE : IDLE_SCALE;
            float targetAlpha = active ? 1.0f        : IDLE_ALPHA;
            float newScale = currentScale + (targetScale - currentScale) * LERP_SPEED;
            float newAlpha = currentAlpha + (targetAlpha - currentAlpha) * LERP_SPEED;
            boolean changed = Math.abs(newScale - currentScale) > 0.0005f
                           || Math.abs(newAlpha - currentAlpha) > 0.0015f;
            currentScale = newScale;
            currentAlpha = newAlpha;
            return changed;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            if (w <= 1 || h <= 1) return;

            int   padX  = playerSide ? 2 : 10;
            int   boxW  = w - padX - 10;
            int   baseH = h;
            int   floorY = baseH + 48;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
            g2.translate(padX, 0);
            if (!playerSide) {
                g2.translate(RIGHT_SLOT_SHIFT_X, 0);
            }

            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, currentAlpha)));

            if (portrait != null) {
                // Base fit: scale portrait to fill the slot height.
                double fitH = baseH * 0.92;
                double fitW = boxW - 2;
                double baseScale = Math.min(fitH / portrait.getHeight(), fitW / portrait.getWidth());

                // Apply the speaking scale around the bottom-center (feet stay put).
                double drawScale = baseScale * currentScale;
                int dw = (int) Math.round(portrait.getWidth()  * drawScale);
                int dh = (int) Math.round(portrait.getHeight() * drawScale);
                int x  = playerSide ? 0 : (boxW - dw) / 2;
                int y  = floorY - dh;

                AffineTransform at = new AffineTransform();
                if (playerSide) {
                    at.translate(x, y);
                    at.scale(drawScale, drawScale);
                } else {
                    at.translate(x + dw, y);
                    at.scale(-drawScale, drawScale);
                }
                g2.drawImage(portrait, at, null);
            } else {
                if (!playerSide) {
                    Graphics2D g3 = (Graphics2D) g2.create();
                    g3.translate(boxW, 0);
                    g3.scale(-1, 1);
                    drawPlaceholder(g3, boxW, baseH, true, placeholderTint);
                    g3.dispose();
                } else {
                    drawPlaceholder(g2, boxW, baseH, true, placeholderTint);
                }
            }

            g2.setComposite(original);
            g2.dispose();
        }

        private static void drawPlaceholder(Graphics2D g2, int boxW, int boxH, boolean leftWeighted, Color tint) {
            Color base = tint != null ? tint : new Color(120, 110, 140);
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 55));
            g2.fill(new RoundRectangle2D.Float(0, 0, boxW, boxH, 14, 14));
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 220));

            float cx   = leftWeighted ? boxW * 0.22f : boxW / 2f;
            float head = Math.min(boxW, boxH) * (leftWeighted ? 0.40f : 0.32f);
            float hx   = cx - head / 2;
            float hy   = boxH * (leftWeighted ? 0.10f : 0.16f);
            g2.fill(new Ellipse2D.Float(hx, hy, head, head));
            float bodyW = head * 1.40f;
            float bodyH = boxH * (leftWeighted ? 0.30f : 0.24f);
            float bx    = cx - bodyW / 2;
            float by    = hy + head * 0.70f;
            g2.fill(new RoundRectangle2D.Float(bx, by, bodyW, bodyH, 10, 10));
        }
    }
}

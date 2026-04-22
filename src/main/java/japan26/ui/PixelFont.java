package japan26.ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loads a pixel-style font from resources with a safe fallback.
 */
public final class PixelFont {
    private static final String FONT_PATH = "/japan26/fonts/PressStart2P-Regular.ttf";
    private static Font baseFont;

    private PixelFont() {
    }

    public static Font regular(float size) {
        return loadBaseFont().deriveFont(Font.PLAIN, size);
    }

    public static Font bold(float size) {
        return loadBaseFont().deriveFont(Font.BOLD, size);
    }

    private static Font loadBaseFont() {
        if (baseFont != null) {
            return baseFont;
        }
        try (InputStream in = PixelFont.class.getResourceAsStream(FONT_PATH)) {
            if (in != null) {
                baseFont = Font.createFont(Font.TRUETYPE_FONT, in);
                return baseFont;
            }
        } catch (FontFormatException | IOException ignored) {
            // Fallback below
        }
        baseFont = new Font("Monospaced", Font.PLAIN, 12);
        return baseFont;
    }
}

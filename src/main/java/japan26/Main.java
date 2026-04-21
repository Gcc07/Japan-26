package japan26;

import japan26.engine.SceneManager;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Swing version of the app.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SceneManager.init();
            SceneManager.showMainMenu();
        });
    }
}

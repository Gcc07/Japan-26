package japan26.minigame;

import javax.swing.JFrame;

/**
 * Interface every minigame must implement.
 * To add a minigame:
 *   1. Create a class that implements this interface.
 *   2. Register it in MinigameRegistry with a unique key.
 *   3. Reference that key in a StoryScene via .thenMinigame("key").
 */
public interface Minigame {

    /**
     * Called by SceneManager to launch the minigame.
     * The implementation should set up its Swing UI and attach it to the frame.
     */
    void start(JFrame frame);

    /**
     * Called by the minigame when the player has finished.
     * SceneManager sets this before calling start().
     */
    void setOnComplete(Runnable callback);
}

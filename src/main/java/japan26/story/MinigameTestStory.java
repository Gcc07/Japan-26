package japan26.story;

import japan26.model.StoryScene;

import java.util.List;

import static japan26.model.Character.NARRATOR;
import static japan26.model.Character.PLAYER;

/**
 * Dedicated story flow for testing minigame hooks in sequence.
 */
public final class MinigameTestStory {
    private MinigameTestStory() {
    }

    public static List<StoryScene> buildStory() {
        return List.of(
                new StoryScene("mini_test_intro", "/japan26/images/Skyline.jpg")
                        .say(NARRATOR, "Minigame Test Route.")
                        .say(NARRATOR, "This path exists so you can wire and tune minigames quickly.")
                        .say(PLAYER, "Let's test the first one.")
                        .thenMinigame("photo_focus"),

                new StoryScene("mini_test_mid_1", "/japan26/images/Crossing 2.jpg")
                        .say(NARRATOR, "Good. Flow resumed after minigame 1.")
                        .say(NARRATOR, "Score above threshold in that rhythm game to earn phil collins drumsticks.")
                        .say(PLAYER, "Next test checkpoint.")
                        .thenMinigame("street_quicktime"),

                new StoryScene("mini_test_mid_2", "/japan26/images/Alleyway1.jpg")
                        .say(NARRATOR, "Second hook passed.")
                        .say(PLAYER, "One more to go.")
                        .thenMinigame("memory_snap"),

                new StoryScene("mini_test_outro", "/japan26/images/Skytree.jpg")
                        .say(NARRATOR, "All minigame checkpoints completed.")
                        .say(NARRATOR, "You can now replace placeholder keys with real implementations.")
        );
    }
}

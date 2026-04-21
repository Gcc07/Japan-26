package japan26.story;

import japan26.model.Character;
import japan26.model.StoryScene;

import java.util.List;

import static japan26.model.Character.NARRATOR;
import static japan26.model.Character.PLAYER;

/**
 * ═══════════════════════════════════════════════════════════════
 *  YOUR STORY SCRIPT  –  Japan 26
 * ═══════════════════════════════════════════════════════════════
 *
 *  Write your story here.  Each StoryScene is one location/moment.
 *
 *  QUICK REFERENCE
 *  ───────────────
 *  Add a line:
 *      .say(NARRATOR, "Narration text here.")
 *      .say(PLAYER,   "Something you say.")
 *      .say(MY_CHAR,  "Something a character says.")
 *
 *  Swap background on a specific line:
 *      .sayWith(NARRATOR, "You arrive at the shrine.", "/japan26/images/shrine.jpg")
 *
 *  Add a character:
 *      private static final Character MY_CHAR = Character.named("Name", "#hexcolor");
 *
 *  Attach a minigame after a scene ends:
 *      .thenMinigame("my_minigame_key")
 *
 *  Background images → src/main/resources/japan26/images/
 *  Reference them as → "/japan26/images/filename.jpg"
 * ═══════════════════════════════════════════════════════════════
 */
public class StoryData {

    // ── Add your characters here ────────────────────────────────────────────
    // private static final Character FRIEND = Character.named("Friend", "#aaddff");

    // ── Your story ──────────────────────────────────────────────────────────
    public static List<StoryScene> buildStory() {
        return List.of(

            new StoryScene("scene_1", "/japan26/images/Skyline.jpg")
                .say(NARRATOR, "Your story starts here.")
                .say(PLAYER,   "Replace this with your own dialogue.")

        );
    }
}

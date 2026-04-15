package japan26.story;

import japan26.model.Character;
import japan26.model.StoryScene;

import java.util.List;

import static japan26.model.Character.NARRATOR;
import static japan26.model.Character.PLAYER;

/**
 * ═══════════════════════════════════════════════════════════════
 *  STORY SCRIPT  –  Japan 26
 * ═══════════════════════════════════════════════════════════════
 *
 *  This is where you write your story.  Each StoryScene maps to a
 *  location / moment.  Use .say() to add dialogue and .sayWith() to
 *  swap the background at the same time.
 *
 *  To add a character:
 *      Character HANA = Character.named("Hana", "#ff9eb5");
 *
 *  To attach a minigame after a scene:
 *      .thenMinigame("shrine_puzzle")
 *  (then register the minigame in MinigameRegistry)
 *
 *  Background images go in:  src/main/resources/japan26/images/
 *  Reference them as:        "/japan26/images/your_file.jpg"
 * ═══════════════════════════════════════════════════════════════
 */
public class StoryData {

    // ── Characters ─────────────────────────────────────────────────────────
    private static final Character HANA   = Character.named("Hana",  "#ff9eb5");
    private static final Character SENSEI = Character.named("Sensei","#88ddaa");

    // ── Story ───────────────────────────────────────────────────────────────
    public static List<StoryScene> buildStory() {
        return List.of(

            // ── Act 1: Arrival ────────────────────────────────────────────
            new StoryScene("arrival", "/japan26/images/placeholder_bg.png")
                .say(NARRATOR, "The bullet train eases to a stop.")
                .say(NARRATOR, "Tokyo Station. Summer, 2026.")
                .say(PLAYER,   "I can't believe I'm finally here.")
                .say(NARRATOR, "The platform hums with quiet energy — " +
                               "a thousand small sounds weaving together like music."),

            // ── Act 2: Shibuya Crossing ───────────────────────────────────
            new StoryScene("shibuya", "/japan26/images/placeholder_bg.png")
                .say(NARRATOR, "Shibuya Crossing. Five lanes of pedestrians flood the intersection.")
                .say(PLAYER,   "There are so many people, and yet... everyone just flows.")
                .say(HANA,     "First time here?")
                .say(PLAYER,   "Is it that obvious?")
                .say(HANA,     "You have the look. Don't worry — everyone gets it.")
                // Attach a minigame here when you're ready:
                // .thenMinigame("crowd_navigation")
                ,

            // ── Act 3: Senso-ji Temple ────────────────────────────────────
            new StoryScene("sensoji", "/japan26/images/placeholder_bg.png")
                .say(NARRATOR, "Asakusa. The Senso-ji gate towers overhead.")
                .say(SENSEI,   "Every stone here has been touched by a million hands.")
                .say(PLAYER,   "It feels older than anything I've ever seen.")
                .say(SENSEI,   "That is because it is. Come — we have much to explore."),

            // ── Act 4: To Be Continued ────────────────────────────────────
            new StoryScene("end_act1", "/japan26/images/placeholder_bg.png")
                .say(NARRATOR, "The first day draws to a close.")
                .say(NARRATOR, "Tomorrow: deeper into the mystery.")
                .say(NARRATOR, "— End of Act 1 —")
        );
    }
}

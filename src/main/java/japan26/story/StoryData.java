package japan26.story;

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
 *  Add dialogue choices (path stays linear, responses vary):
 *      .choose("mood", "How do you respond?", "Confident", "Nervous")
 *      .sayByChoice(PLAYER, "mood", "Let's go.",
 *          "Confident", "I've got this.",
 *          "Nervous",   "I hope this goes well...")
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
    /** Beats drafted in {@code mystory.txt} at the project root. */
    public static List<StoryScene> buildStory() {
        return List.of(

            new StoryScene("plane_opening", "/japan26/images/AirPlaneMidAir.JPG")
                .say(NARRATOR, "Okay. This is the story.")
                .say(NARRATOR, "It's spring 2026. You're going to Japan for senior trip, getting away from it all.")
                .say(PLAYER, "This is gonna be so dope i'm gonna eat so much food and see so much cool stuff")
                .sayWith(NARRATOR, "", "/japan26/images/AirPlane.jpg")
                .say(PLAYER, "I mean, just look at this plane. This thing is massive.")
                .say(PLAYER, "That hour plane ride to Canada kinda sucked but these next 12 hours are gonna be GREAT!")
                .say(PLAYER, "Kinda nuts... I'm a little tired tho.")
                .say(NARRATOR, "(Fade to black)")
                .sayWith(NARRATOR,
                        "You sleep, and later arrive at the Tokyo Airport. You maneuver your way into the subway line, groggy from the jetlag.",
                        "__BLACK__")
                .say(PLAYER, "Yo I'm mad tired. I feel like I'm Karim in 2006.")
                .say(NARRATOR, "You wait for the train, eyes closing.")

        );
    }
}

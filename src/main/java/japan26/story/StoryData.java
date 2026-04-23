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
    private static final Character RYO = Character.namedWithPortrait(
            "Ryo",
            "#8fd5ff",
            "/japan26/Sprites/player11.png"
    );

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
                // black screen narration (lines 18-20)
                .sayWith(NARRATOR, "You sleep, and later arrive at the Tokyo Airport.", "__BLACK__")
                .say(NARRATOR, "You maneuver your way into the subway line, groggy from the jetlag.")
                // *cut to TrainStation1* (line 22)
                .sayWith(PLAYER,
                        "Yo I'm mad tired. I feel like I'm Karim in 2006.",
                        "/japan26/images/TrainStation1.jpg")
                .say(NARRATOR, "You wait for the train, eyes closing.")
                // *Cut to skyline.jpg* (line 28)
                .sayWith(NARRATOR, "You leave the train and go to your AirBNB.", "/japan26/images/Skyline.jpg")
                .say(PLAYER, "The view here is crazy.")
                .say(PLAYER, "I should probably head to bed.")
                // !CHOICE (line 36)
                .choose("night_choice", "What do you do?", "Go to bed", "Wander the streets")
                .sayByChoice(NARRATOR, "night_choice", "You collapse into your bed, exhausted.",
                        "Go to bed",          "You collapse into your bed, exhausted.",
                        "Wander the streets", "You collapsed at the door.")
                // *Fade to black* (line 40)
                .say(NARRATOR, "(Fade to black)")
                // dream narration on black (line 42)
                .sayWith(NARRATOR, "You dream of all the movies you watched on the plane.", "__BLACK__")
                // *Cut to Alleyway1* (line 45)
                .sayWith(PLAYER, "Good morning Japan.", "/japan26/images/Alleyway1.jpg")
                .say(PLAYER, "I'm kinda thirsty what should I get.")
                // !CHOICE - Matcha / Coffee (line 51)
                // First response: bg swaps to the chosen drink image
                .choose("drink_choice", "What do you get?", "Matcha", "Coffee")
                .sayByChoiceWith(PLAYER, "drink_choice", "Matcha it is-",
                        "Matcha", "Matcha it is-",           "/japan26/images/Matcha.jpg",
                        "Coffee", "Coffee it is.",           "/japan26/images/Coffee.jpg")
                // Matcha: they're out → coffee. Coffee: good coffee.
                .sayByChoiceWith(PLAYER, "drink_choice", "They're out. Coffee it is, I guess.",
                        "Matcha", "They're out. Coffee it is, I guess.", "/japan26/images/Coffee.jpg",
                        "Coffee", "Mmm good coffee.",                   "/japan26/images/Coffee.jpg")
                // *Change to Akiabara* + arcade crossover
                .sayWith(NARRATOR, "You wander into Akihabara, neon buzzing everywhere.", "/japan26/images/Akiabara.jpg")
                .say(PLAYER, "Yo this place is insane. I could stay here all day.")
                .choose("arcade_choice", "What do you hit first?", "Claw Machine", "Rhythm Game")
                .sayByChoice(NARRATOR, "arcade_choice", "You line up at a claw machine.",
                        "Claw Machine", "You line up at a claw machine.",
                        "Rhythm Game",  "You hear a drum rhythm game calling your name.")
                .thenMinigameByChoice("arcade_choice",
                        "Claw Machine", "claw_machine",
                        "Rhythm Game",  "rhythm_game"),

            new StoryScene("arcade_aftergame", "/japan26/images/Game.jpg")
                .sayByChoice(NARRATOR, "arcade_choice", "You step away from the machine with a dumb grin.",
                        "Claw Machine", "You step away from the machine with a dumb grin.",
                        "Rhythm Game",  "Your hands are cooked, but that run was clean.")
                .say(PLAYER, "Okay yeah, Japan is delivering so far."),

            new StoryScene("akiba_night_walk", "/japan26/images/Road1.jpg")
                .say(NARRATOR, "Outside, Akihabara is still glowing like it's noon.")
                .say(PLAYER, "I should head back soon... but one more lap couldn't hurt.")
                .say(NARRATOR, "You drift with the crowd, pockets lighter, mood way better.")
                .say(PLAYER, "Yeah. This trip is exactly what I needed."),

            new StoryScene("rainy_crossroads_meet", "/japan26/images/Rainy Crossroads.jpg")
                .say(NARRATOR, "Rain starts coming down hard by the time you hit the crossroads.")
                .say(RYO, "Yo. You're the one who cleared that rhythm machine, right?")
                .say(PLAYER, "Yeah, that was me.")
                .say(RYO, "Respect. I've been hunting Phil's drumsticks forever.")
                .choose("drumsticks_offer",
                        "What do you do?",
                        "Give him Phil's drumsticks",
                        "Keep them",
                        "I don't have them")
                .sayByChoice(RYO, "drumsticks_offer", "No stress. If you find them, come back.",
                        "Give him Phil's drumsticks", "No way... for real? You're a legend. Take this skin unlock.",
                        "Keep them",                  "Fair call. I'd probably keep them too.",
                        "I don't have them",          "No stress. If you find them, come back.")
                .sayByChoice(PLAYER, "drumsticks_offer", "Maybe next run.",
                        "Give him Phil's drumsticks", "Worth it. That fit is clean.",
                        "Keep them",                  "Can't part with them yet.",
                        "I don't have them",          "I'll keep an eye out.")

        );
    }
}

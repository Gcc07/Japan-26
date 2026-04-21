package japan26.story;

import japan26.model.Character;
import japan26.model.StoryScene;

import java.util.List;

import static japan26.model.Character.NARRATOR;
import static japan26.model.Character.PLAYER;

/**
 * The AI-generated test story — kept here for reference.
 * Accessible from the main menu via "Test Story".
 */
public class TestStory {

    // ── Characters ─────────────────────────────────────────────────────────
    private static final Character HANA    = Character.named("Hana",    "#ff9eb5");
    private static final Character SENSEI  = Character.named("Sensei",  "#88ddaa");
    private static final Character KENJI   = Character.named("Kenji",   "#88bbff");
    private static final Character OBASAN  = Character.named("Obasan",  "#ddbb88");

    // ── Story ───────────────────────────────────────────────────────────────
    public static List<StoryScene> buildStory() {
        return List.of(

            // ══════════════════════════════════════════════════════════════
            //  ACT 1 — ARRIVAL
            // ══════════════════════════════════════════════════════════════

            new StoryScene("title_card", "/japan26/images/Skyline.jpg")
                .say(NARRATOR, "Japan.")
                .say(NARRATOR, "Summer, 2026.")
                .say(NARRATOR, "You saved for two years for this trip.")
                .say(NARRATOR, "You have no plan. Just a backpack, a camera, and three weeks."),

            new StoryScene("shinkansen", "/japan26/images/AirPlaneMidAir.JPG")
                .say(NARRATOR, "The Shinkansen cuts through mountains at 320 kilometers per hour.")
                .say(PLAYER,   "I keep waiting for it to feel real.")
                .say(NARRATOR, "Outside, terraced rice paddies blur past. A glimpse of Mt. Fuji — gone before you can raise your camera.")
                .say(PLAYER,   "...Was that—?")
                .say(NARRATOR, "It was."),

            new StoryScene("tokyo_station", "/japan26/images/TrainStation1.jpg")
                .say(NARRATOR, "Tokyo Station. The bullet train eases to a stop with impossible smoothness.")
                .say(PLAYER,   "Okay. Okay. I'm here.")
                .say(NARRATOR, "The platform is a river of people moving in perfect, unhurried order.")
                .say(NARRATOR, "Nobody bumps into anyone. Nobody yells. It's the loudest quiet you've ever heard.")
                .say(PLAYER,   "Where do I even start?"),

            // ══════════════════════════════════════════════════════════════
            //  ACT 2 — SHIBUYA
            // ══════════════════════════════════════════════════════════════

            new StoryScene("shibuya_day", "/japan26/images/Crossing 2.jpg")
                .say(NARRATOR, "Shibuya. You emerge from the subway into blinding afternoon light.")
                .say(NARRATOR, "The crossing stretches in every direction — a sea of umbrellas and sneakers.")
                .say(PLAYER,   "I've seen this in a hundred photos and it STILL looks unreal.")
                .say(HANA,     "Are you going to stand there or actually cross?")
                .say(PLAYER,   "Sorry — first time.")
                .say(HANA,     "I could tell. Come on, the light's about to change."),

            new StoryScene("shibuya_coffee", "/japan26/images/Coffee.jpg")
                .say(NARRATOR, "She leads you to a tiny coffee stand tucked between two vending machines.")
                .say(HANA,     "Where are you from?")
                .say(PLAYER,   "The States. You?")
                .say(HANA,     "Tokyo, born and raised. I'm Hana.")
                .say(PLAYER,   "Gabe. I have absolutely no idea where I'm going today.")
                .say(HANA,     "Perfect. I know every good place that isn't in any guidebook.")
                .say(HANA,     "I'll show you the real Shibuya, if you want.")
                .say(PLAYER,   "...Yeah. I'd really like that."),

            // ══════════════════════════════════════════════════════════════
            //  ACT 3 — ASAKUSA & THE MYSTERY
            // ══════════════════════════════════════════════════════════════

            new StoryScene("asakusa_gate", "/japan26/images/Sensi Jo.jpg")
                .say(NARRATOR, "Asakusa. The Kaminarimon gate looms — a massive red lantern hanging at its center.")
                .say(PLAYER,   "It feels older than anything I've ever been near.")
                .say(HANA,     "Senso-ji has stood here since 645 AD. Give or take a few rebuilds.")
                .say(NARRATOR, "An elderly man sits on the temple steps, staring at a folded paper.")
                .say(NARRATOR, "He looks up at you. Specifically at you."),

            new StoryScene("sensei_meeting", "/japan26/images/Sensi Jo.jpg")
                .say(SENSEI,   "You. You have the camera.")
                .say(PLAYER,   "...Me?")
                .say(SENSEI,   "I am Tanaka. I have been waiting here for three days.")
                .say(HANA,     "Sensei Tanaka? The photographer?")
                .say(SENSEI,   "Retired. Mostly.")
                .say(SENSEI,   "There is something hidden in this city. Something meant to be found by someone with fresh eyes.")
                .say(PLAYER,   "What kind of something?")
                .say(SENSEI,   "The kind you photograph. Come back tomorrow. Bring the camera."),

            new StoryScene("asakusa_night", "/japan26/images/RainyDay.jpg")
                .say(NARRATOR, "You and Hana walk the backstreets of Asakusa as lanterns flicker on.")
                .say(HANA,     "He's a little strange, but Tanaka-sensei is the real deal. His photos are in museums.")
                .say(PLAYER,   "What do you think he meant — something hidden?")
                .say(HANA,     "No idea. But you only just got here and already an old master wants to meet you tomorrow.")
                .say(HANA,     "That has to mean something.")
                .say(PLAYER,   "Or he's just lonely.")
                .say(HANA,     "...")
                .say(PLAYER,   "...I'll be there tomorrow."),

            // ══════════════════════════════════════════════════════════════
            //  ACT 4 — DEEPER IN
            // ══════════════════════════════════════════════════════════════

            new StoryScene("yanaka_alley", "/japan26/images/Alleyway1.jpg")
                .say(NARRATOR, "The next morning. Yanaka — one of the few old neighborhoods that survived the war.")
                .say(NARRATOR, "Sensei leads you through narrow lanes lined with cats and moss-covered graves.")
                .say(SENSEI,   "This city has layers. Most tourists only ever see the top one.")
                .say(PLAYER,   "What's underneath?")
                .say(SENSEI,   "Stories. Belonging to people who left no other record.")
                .say(SENSEI,   "We are going to find one of them."),

            new StoryScene("old_shop", "/japan26/images/EnclosedHallway.jpg")
                .say(NARRATOR, "A tiny antique shop. The sign is almost illegible.")
                .say(NARRATOR, "Inside: cameras. Hundreds of them. Every era, every format.")
                .say(OBASAN,   "Irasshaimase.")
                .say(SENSEI,   "Her name is Michiko. She has run this shop for fifty years.")
                .say(OBASAN,   "You are American? You have good eyes. Like a photographer.")
                .say(PLAYER,   "I'm trying to be.")
                .say(OBASAN,   "Here.")
                .say(NARRATOR, "She places a small, battered film camera on the counter. An Olympus Trip 35. Old.")
                .say(OBASAN,   "This camera has not been used in forty years. It needs to see Japan again."),

            new StoryScene("kenji_encounter", "/japan26/images/Road1.jpg")
                .say(NARRATOR, "Outside the shop, a young man leans against the wall — watching.")
                .say(KENJI,    "You took the camera.")
                .say(PLAYER,   "...Yeah?")
                .say(KENJI,    "That was my grandfather's. He disappeared in 1987. Nobody knows what happened to him.")
                .say(KENJI,    "They say his last photos might still be inside it.")
                .say(PLAYER,   "There's still film in here?")
                .say(SENSEI,   "That is what we need to find out."),

            // ══════════════════════════════════════════════════════════════
            //  ACT 5 — END OF ACT 1
            // ══════════════════════════════════════════════════════════════

            new StoryScene("end_act1", "/japan26/images/Skytree.jpg")
                .say(NARRATOR, "Three days into your trip.")
                .say(NARRATOR, "You came to Japan for sightseeing.")
                .say(NARRATOR, "Somehow you've ended up holding a forty-year-old mystery in your hands.")
                .say(PLAYER,   "I need to find somewhere to develop this film.")
                .say(HANA,     "I know a place. Old school darkroom, down in Shimokitazawa.")
                .say(KENJI,    "Whatever is on those photos... I need to know.")
                .say(NARRATOR, "You look at the camera. Small, dented, unremarkable.")
                .say(NARRATOR, "Somewhere inside it, Japan is waiting to be seen again.")
                .say(NARRATOR, "— End of Act 1 —")
        );
    }
}

package japan26.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A named story scene composed of an ordered list of DialogueLines.
 * Uses a fluent builder API so story scripts stay readable (Ren'Py style):
 *
 *   new StoryScene("arrival", "images/tokyo_station.jpg")
 *       .say(NARRATOR, "The bullet train slows...")
 *       .say(PLAYER,   "I can't believe I'm finally here.");
 */
public class StoryScene {

    private final String              id;
    private final String              defaultBackground; // resource path
    private final List<DialogueLine>  lines = new ArrayList<>();

    /** Optional: a minigame key to launch after this scene ends (null = none). */
    private String followUpMinigame = null;
    /** Optional: launch minigame based on a prior choice in this scene. */
    private String choiceMinigameChoiceId = null;
    private final Map<String, String> choiceMinigameByOption = new HashMap<>();

    public StoryScene(String id, String defaultBackground) {
        this.id                = id;
        this.defaultBackground = defaultBackground;
    }

    // ── Fluent builder ────────────────────────────────────────────────────────

    /** Add a line using the scene's current background. */
    public StoryScene say(Character character, String text) {
        lines.add(new DialogueLine(character, text));
        return this;
    }

    /** Add a line AND swap the background at that moment. */
    public StoryScene sayWith(Character character, String text, String backgroundPath) {
        lines.add(new DialogueLine(character, text, backgroundPath));
        return this;
    }

    /** Present a player choice prompt (story path remains unchanged). */
    public StoryScene choose(String choiceId, String prompt, String... options) {
        lines.add(DialogueLine.choicePrompt(choiceId, prompt, options));
        return this;
    }

    /**
     * Add a spoken line whose text changes based on a prior choice.
     * responsePairs should be option/text pairs:
     *   "Confident", "Let's do this.", "Nervous", "Uh...okay."
     */
    public StoryScene sayByChoice(
            Character character,
            String choiceId,
            String defaultText,
            String... responsePairs
    ) {
        Map<String, String> responses = new HashMap<>();
        for (int i = 0; i + 1 < responsePairs.length; i += 2) {
            responses.put(responsePairs[i], responsePairs[i + 1]);
        }
        lines.add(DialogueLine.choiceResponse(character, choiceId, defaultText, responses));
        return this;
    }

    /**
     * Like {@link #sayByChoice} but also swaps backgrounds per choice.
     * triplets should be option/text/bgPath groups:
     *   "Matcha", "Matcha it is-", "/japan26/images/Matcha.jpg",
     *   "Coffee", "Coffee it is.", "/japan26/images/Coffee.jpg"
     */
    public StoryScene sayByChoiceWith(
            Character character,
            String choiceId,
            String defaultText,
            String... triplets
    ) {
        Map<String, String> responses = new HashMap<>();
        Map<String, String> bgs       = new HashMap<>();
        for (int i = 0; i + 2 < triplets.length; i += 3) {
            responses.put(triplets[i],     triplets[i + 1]);
            bgs.put(      triplets[i],     triplets[i + 2]);
        }
        lines.add(DialogueLine.choiceResponseWithBg(character, choiceId, defaultText, responses, bgs));
        return this;
    }

    /** Mark that a minigame should launch after this scene completes. */
    public StoryScene thenMinigame(String minigameKey) {
        this.followUpMinigame = minigameKey;
        return this;
    }

    /**
     * Launches a minigame at scene end based on a selected choice option.
     * Pairs are option/minigameKey, e.g.:
     *   "Claw Machine", "claw_machine", "Rhythm Game", "rhythm_game"
     */
    public StoryScene thenMinigameByChoice(String choiceId, String... optionKeyPairs) {
        this.choiceMinigameChoiceId = choiceId;
        this.choiceMinigameByOption.clear();
        for (int i = 0; i + 1 < optionKeyPairs.length; i += 2) {
            choiceMinigameByOption.put(optionKeyPairs[i], optionKeyPairs[i + 1]);
        }
        return this;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String             getId()               { return id; }
    public String             getDefaultBackground(){ return defaultBackground; }
    public List<DialogueLine> getLines()            { return lines; }
    public String             getFollowUpMinigame() { return followUpMinigame; }
    public boolean            hasMinigame()         { return followUpMinigame != null; }
    public boolean            hasChoiceMinigame()   {
        return choiceMinigameChoiceId != null && !choiceMinigameByOption.isEmpty();
    }
    public String resolveChoiceMinigame(String selectedOption) {
        if (selectedOption == null || choiceMinigameByOption.isEmpty()) return null;
        return choiceMinigameByOption.get(selectedOption);
    }
    public String getChoiceMinigameChoiceId() { return choiceMinigameChoiceId; }
}

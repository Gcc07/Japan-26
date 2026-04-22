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

    /** Mark that a minigame should launch after this scene completes. */
    public StoryScene thenMinigame(String minigameKey) {
        this.followUpMinigame = minigameKey;
        return this;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String             getId()               { return id; }
    public String             getDefaultBackground(){ return defaultBackground; }
    public List<DialogueLine> getLines()            { return lines; }
    public String             getFollowUpMinigame() { return followUpMinigame; }
    public boolean            hasMinigame()         { return followUpMinigame != null; }
}

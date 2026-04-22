package japan26.model;

import java.util.List;
import java.util.Map;

/**
 * A single beat in the story: who speaks, what they say, and an optional
 * background swap.  Pass null for backgroundPath to keep the current background.
 */
public class DialogueLine {

    private final Character character;
    private final String    text;
    private final String    backgroundPath; // resource path, or null = no change
    private final String    choiceId;
    private final List<String> choiceOptions;
    private final Map<String, String> choiceResponses;

    public DialogueLine(Character character, String text) {
        this(character, text, null, null, List.of(), Map.of());
    }

    public DialogueLine(Character character, String text, String backgroundPath) {
        this(character, text, backgroundPath, null, List.of(), Map.of());
    }

    private DialogueLine(
            Character character,
            String text,
            String backgroundPath,
            String choiceId,
            List<String> choiceOptions,
            Map<String, String> choiceResponses
    ) {
        this.character      = character;
        this.text           = text;
        this.backgroundPath = backgroundPath;
        this.choiceId       = choiceId;
        this.choiceOptions  = choiceOptions;
        this.choiceResponses = choiceResponses;
    }

    /** Factory for a line that presents selectable choices to the player. */
    public static DialogueLine choicePrompt(String choiceId, String prompt, String... options) {
        return new DialogueLine(
                Character.NARRATOR,
                prompt,
                null,
                choiceId,
                List.of(options),
                Map.of()
        );
    }

    /** Factory for a spoken line that varies based on a prior choice. */
    public static DialogueLine choiceResponse(
            Character character,
            String choiceId,
            String defaultText,
            Map<String, String> responsesByOption
    ) {
        return new DialogueLine(
                character,
                defaultText,
                null,
                choiceId,
                List.of(),
                responsesByOption
        );
    }

    public Character getCharacter()      { return character; }
    public String    getText()           { return text; }
    public String    getBackgroundPath() { return backgroundPath; }
    public boolean   changesBackground() { return backgroundPath != null; }
    public boolean   hasChoices()        { return !choiceOptions.isEmpty(); }
    public List<String> getChoiceOptions() { return choiceOptions; }
    public String getChoiceId() { return choiceId; }
    public boolean hasChoiceResponses() { return !choiceResponses.isEmpty(); }
    public String getTextForChoice(String selectedOption) {
        if (selectedOption == null) return text;
        return choiceResponses.getOrDefault(selectedOption, text);
    }
}

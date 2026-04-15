package japan26.model;

/**
 * A single beat in the story: who speaks, what they say, and an optional
 * background swap.  Pass null for backgroundPath to keep the current background.
 */
public class DialogueLine {

    private final Character character;
    private final String    text;
    private final String    backgroundPath; // resource path, or null = no change

    public DialogueLine(Character character, String text) {
        this(character, text, null);
    }

    public DialogueLine(Character character, String text, String backgroundPath) {
        this.character      = character;
        this.text           = text;
        this.backgroundPath = backgroundPath;
    }

    public Character getCharacter()      { return character; }
    public String    getText()           { return text; }
    public String    getBackgroundPath() { return backgroundPath; }
    public boolean   changesBackground() { return backgroundPath != null; }
}

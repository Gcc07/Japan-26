package japan26.model;

/**
 * Represents a speaking character in the story.
 * Use the pre-defined constants or create custom characters with a name and display color.
 */
public class Character {

    public static final Character NARRATOR = new Character("", "#c8c8c8", null);
    public static final Character PLAYER   = new Character("Gabe", "#f0c060", null);
    public static final Character NPC      = new Character("???", "#88ccff", null);

    private final String name;
    private final String color; // CSS hex color for the name label
    /** Optional class-path resource for dialogue portraits, e.g. {@code "/japan26/Sprites/npc.png"}. */
    private final String portraitResourcePath;

    public Character(String name, String color) {
        this(name, color, null);
    }

    public Character(String name, String color, String portraitResourcePath) {
        this.name  = name;
        this.color = color;
        this.portraitResourcePath = portraitResourcePath;
    }

    public String getName()  { return name; }
    public String getColor() { return color; }

    /** May be null: use player preset sprite for {@link #PLAYER}, or a colored placeholder for others. */
    public String getPortraitResourcePath() {
        return portraitResourcePath;
    }

    /** Convenience factory so story scripts read naturally. */
    public static Character named(String name, String color) {
        return new Character(name, color, null);
    }

    /** Story character with a portrait image on the class-path. */
    public static Character namedWithPortrait(String name, String color, String portraitResourcePath) {
        return new Character(name, color, portraitResourcePath);
    }
}

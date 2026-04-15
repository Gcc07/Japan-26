package japan26.model;

/**
 * Represents a speaking character in the story.
 * Use the pre-defined constants or create custom characters with a name and display color.
 */
public class Character {

    public static final Character NARRATOR = new Character("", "#c8c8c8");
    public static final Character PLAYER   = new Character("Gabe", "#f0c060");
    public static final Character NPC      = new Character("???", "#88ccff");

    private final String name;
    private final String color; // CSS hex color for the name label

    public Character(String name, String color) {
        this.name  = name;
        this.color = color;
    }

    public String getName()  { return name; }
    public String getColor() { return color; }

    /** Convenience factory so story scripts read naturally. */
    public static Character named(String name, String color) {
        return new Character(name, color);
    }
}

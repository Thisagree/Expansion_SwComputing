package engine.augment;

public class Augment {
    public final String name;
    public final String description;
    public final AugmentEffect effect;

    /**
     * Creates an augment with its display name, description, and effect logic.
     */
    public Augment(String name, String description, AugmentEffect effect) {
        this.name = name;
        this.description = description;
        this.effect = effect;
    }
}
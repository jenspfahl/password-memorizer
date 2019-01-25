package de.jepfa.obfusser.model;

/**
 * All representations of supported placeholders.
 *
 * @author Jens Pfahl
 */
public class NumberedPlaceholder {

    private static final char NUMBER_1 = '\u2460';   // "\u2460" '①'

    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 20;

    private char representation;
    private int number;

    private NumberedPlaceholder(char representation, int number) {
        this.representation = representation;
        this.number = number;
    }

    public char getRepresentation() {
        return representation;
    }

    public Integer getPlaceholderNumber() {
        return number;
    }

    public String toRepresentation() {
        return new String(new char[]{getRepresentation()});
    }

    @Override
    public String toString() {
        return toRepresentation();
    }


    public static NumberedPlaceholder fromPlaceholderNumber(int placeholder) {
        if (placeholder >= MIN_NUMBER && placeholder <= MAX_NUMBER) {
            return new NumberedPlaceholder(getCharForNumber(placeholder), placeholder);
        }
        throw new IllegalStateException("Unknown placeholder number: " + placeholder);
    }


    public static int count() {
        return MAX_NUMBER;
    }

    private static char getCharForNumber(int placeholder) {
        return (char)(((int)NUMBER_1) + placeholder - 1);
    }

}

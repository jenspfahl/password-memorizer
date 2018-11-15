package de.jepfa.obfusser.model;

/**
 * All representations of supported placeholders.
 *
 * @author Jens Pfahl
 */
public enum NumberedPlaceholder {
    PLACEHOLDER_1('\u2460'),   // "\u2460" '①'
    PLACEHOLDER_2('\u2461'),   // "\u2460" '②'
    PLACEHOLDER_3('\u2462'),   // "\u2460" '③'
    PLACEHOLDER_4('\u2463'),   // "\u2460" '④'
    PLACEHOLDER_5('\u2464'),   // "\u2460" '⑤'
    PLACEHOLDER_6('\u2465'),   // "\u2460" '⑥'
    PLACEHOLDER_7('\u2466'),   // "\u2460" '⑦'
    PLACEHOLDER_8('\u2467'),   // "\u2460" '⑧'
    PLACEHOLDER_9('\u2468'),   // "\u2460" '⑨'

    ;

    private char representation;

    private NumberedPlaceholder(char representation) {
        this.representation = representation;
    }

    public char getRepresentation() {
        return representation;
    }

    public Integer getPlaceholderNumber() {
        return ordinal() + 1;
    }

    public String toRepresentation() {
        return new String(new char[]{getRepresentation()});
    }

    @Override
    public String toString() {
        return toRepresentation();
    }


    public static NumberedPlaceholder fromPlaceholderNumber(int placeholder) {
        if (placeholder == 1) {
            return(NumberedPlaceholder.PLACEHOLDER_1);
        }
        else if (placeholder == 2) {
            return(NumberedPlaceholder.PLACEHOLDER_2);
        }
        else if (placeholder == 3) {
            return(NumberedPlaceholder.PLACEHOLDER_3);
        }
        else if (placeholder == 4) {
            return(NumberedPlaceholder.PLACEHOLDER_4);
        }
        else if (placeholder == 5) {
            return(NumberedPlaceholder.PLACEHOLDER_5);
        }
        else if (placeholder == 6) {
            return(NumberedPlaceholder.PLACEHOLDER_6);
        }
        else if (placeholder == 7) {
            return(NumberedPlaceholder.PLACEHOLDER_7);
        }
        else if (placeholder == 8) {
            return(NumberedPlaceholder.PLACEHOLDER_8);
        }
        else if (placeholder == 9) {
            return(NumberedPlaceholder.PLACEHOLDER_9);
        }

        throw new IllegalStateException("Unknown placeholder number: " + placeholder);
    }

}

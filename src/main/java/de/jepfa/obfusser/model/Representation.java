package de.jepfa.obfusser.model;

public class Representation {

    private char lowerChar;
    private char upperChar;
    private char digit;
    private char specialChar;
    private char anyChar;
    private char placeholderChar;

    public Representation(char lowerChar, char upperChar, char digit, char specialChar, char anyChar, char placeholderChar) {
        this.lowerChar = lowerChar;
        this.upperChar = upperChar;
        this.digit = digit;
        this.specialChar = specialChar;
        this.anyChar = anyChar;
        this.placeholderChar = placeholderChar;
    }

    public char getLowerChar() {
        return lowerChar;
    }

    public char getUpperChar() {
        return upperChar;
    }

    public char getDigit() {
        return digit;
    }

    public char getSpecialChar() {
        return specialChar;
    }

    public char getAnyChar() {
        return anyChar;
    }

    public char getPlaceholderChar() {
        return placeholderChar;
    }

    public char getRepresentation(ObfusChar obfusChar) {
        switch (obfusChar) {
            case LOWER_CASE_CHAR: return getLowerChar();
            case UPPER_CASE_CHAR: return getUpperChar();
            case DIGIT: return getDigit();
            case SPECIAL_CHAR: return getSpecialChar();
            case ANY_CHAR: return getAnyChar();
            case PLACEHOLDER: return getPlaceholderChar();
        }
        throw new IllegalStateException("Unknown ObfusChar: " + obfusChar);
    }
}

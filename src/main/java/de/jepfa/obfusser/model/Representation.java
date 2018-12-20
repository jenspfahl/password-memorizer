package de.jepfa.obfusser.model;

import android.os.Build;

public enum Representation {

    DEFAULT_BLOCKS("Blocks (defaut)",
            '\u2584', // '▄'
            '\u2588', // '█'
            getAlternative(Build.VERSION_CODES.M, '\u2B24', '\u25CF'),  // '⬤' / '●'
            getAlternative(Build.VERSION_CODES.LOLLIPOP, '\u2580', '\u25A0'), // '▀' / '■'
            '\u25A0', // '■'
            '\u25EF', // '◯'
            null),

    VIKING("Viking",
            '\u16B4', // ''
            '\u16A1', // ''
            '\u16CA',
            '\u16C9',
            '\u16DC', // ''
            '\u25EF', // '◯'
            Build.VERSION_CODES.M),

    HATCHING("Hatching",
            '\u2591', // ''
            '\u2592', // ''
            '\u2593',
            '\u2594',
            '\u2596', // ''
            '\u25EF', // '◯'
            Build.VERSION_CODES.M),

    BRAILLE("Braille",
            '\u2860', // ''
            '\u28F8', // ''
            '\u28F6',
            '\u2819',
            '\u28FF', // ''
            '\u25EF', // '◯'
            Build.VERSION_CODES.M),

    ;


    private static char getAlternative(int version, char origin, char alternative) {
        return Build.VERSION.SDK_INT >= version ? origin : alternative;
    }

    private String name;
    private char lowerChar;
    private char upperChar;
    private char digit;
    private char specialChar;
    private char anyChar;
    private char placeholderChar;
    private Integer minVersion;

    private Representation(String name, char lowerChar, char upperChar, char digit, char specialChar,
                           char anyChar, char placeholderChar, Integer minVersion) {
        this.name = name;
        this.lowerChar = lowerChar;
        this.upperChar = upperChar;
        this.digit = digit;
        this.specialChar = specialChar;
        this.anyChar = anyChar;
        this.placeholderChar = placeholderChar;
        this.minVersion = minVersion;
    }

    public String getName() {
        return name;
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

    public Integer getMinVersion() {
        return minVersion;
    }

    public boolean isAvailable() {
        return minVersion == null || Build.VERSION.SDK_INT >= minVersion;
    }

    public String getTitle() {
        return getName() + " - " + getLowerChar() + getUpperChar() + getDigit() + getSpecialChar();
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

package de.jepfa.obfusser.model;

import android.os.Build;

public enum Representation {

    DEFAULT_BLOCKS("Blocks (default)",
            '\u2584', // '▄'
            '\u2588', // '█'
            getAlternative(Build.VERSION_CODES.M, '\u2B24', '\u25CF'),  // '⬤' / '●'
            getAlternative(Build.VERSION_CODES.LOLLIPOP, '\u2580', '\u25A0'), // '▀' / '■'
            '\u25A0', // '■'
            null),

    SYMBOLS("Symbols",
            '\u25B2',
            '\u25A0',
            '\u25CF',
            '\u25C6',  // alt: 25BC
            '\u25FE',
            null,
            0.0f,
            32.0f),

    VIKING("Viking",
            '\u16B4',
            '\u16A1',
            '\u16CA',
            '\u16C9',
            '\u16DC',
            Build.VERSION_CODES.O),

    HATCHING("Hatching",
            '\u2591',
            '\u2592',
            '\u2593',
            '\u2594',
            '\u2596',
            Build.VERSION_CODES.M),

    BRAILLE("Braille",
            '\u2860',
            '\u28F8',
            '\u28F6',
            '\u2819',
            '\u28FF',
            Build.VERSION_CODES.M),

    NUMBERS("Numbers",
            '1',
            '3',
            '5',
            '7',
            '0',
            null),

    HEXAGRAM("Hexagram",
            '\u4DD2',
            '\u4DD3',
            '\u4DE8',
            '\u4DEA',
            '\u4DC0',
            Build.VERSION_CODES.O),

    LINES("Lines",
            '\u23CA',
            '\u23C9',
            '\u23CB',
            '\u23CC',
            '\u2014',
            Build.VERSION_CODES.M,
            null,
            22.0f),

    MUSIC("Music",
            '\u15B1',
            '\u15B0',
            '\u15B2',
            '\u15B3',
            '\u2022',
            Build.VERSION_CODES.M,
            null,
            32.0f),

    CHESS("Chess",
            '\u265F',
            '\u265A',
            '\u2658',
            '\u2657',
            '\u2656',
            Build.VERSION_CODES.LOLLIPOP,
            null,
            32.0f),

    RECORDER("Recorder",
            '\u23E9',
            '\u23F9',
            '\u23FA',
            '\u23F8',
            '\u23EB',
            Build.VERSION_CODES.O),

    ;

    private static final char COMMON_PLACEHOLDER_CHAR = '\u25EF'; // '◯'


    private static char getAlternative(int version, char origin, char alternative) {
        return Build.VERSION.SDK_INT >= version ? origin : alternative;
    }

    private String name;
    private char lowerChar;
    private char upperChar;
    private char digit;
    private char specialChar;
    private char anyChar;
    private Integer minVersion;
    private Float letterSpacing;
    private Float textSize;

    private Representation(String name, char lowerChar, char upperChar, char digit, char specialChar,
                           char anyChar, Integer minVersion) {
        this(name, lowerChar, upperChar, digit, specialChar, anyChar, minVersion, null, null);
    }

    private Representation(String name, char lowerChar, char upperChar, char digit, char specialChar,
                           char anyChar, Integer minVersion, Float letterSpacing, Float textSize) {
        this.name = name;
        this.lowerChar = lowerChar;
        this.upperChar = upperChar;
        this.digit = digit;
        this.specialChar = specialChar;
        this.anyChar = anyChar;
        this.minVersion = minVersion;
        this.letterSpacing = letterSpacing;
        this.textSize = textSize;
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
        return COMMON_PLACEHOLDER_CHAR;
    }

    public Integer getMinVersion() {
        return minVersion;
    }

    public Float getLetterSpacing() {
        return letterSpacing;
    }

    public Float getTextSize() {
        return textSize;
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

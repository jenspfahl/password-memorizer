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

    CARDS("Cards",
            '\u2660',
            '\u2665',
            '\u25c6',
            '\u2663',
            '\u2605',
            null,
            null,
            31.0f),

    VIKING("Viking",
            '\u16B4',
            '\u16A1',
            '\u16CA',
            '\u16C9',
            '\u16DC',
            Build.VERSION_CODES.O),

    BARCODE("Barcode",
            '\u258B',
            '\u2589',
            '\u258E',
            '\u2595',
            '\u2588',
            Build.VERSION_CODES.N),

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
            '\u4DFD',
            '\u4DC0',
            Build.VERSION_CODES.O),

    LINES("Lines",
            '\u23CA',
            '\u23C9',
            '\u23CB',
            '\u23CC',
            '\u23B9',
            Build.VERSION_CODES.M,
            null,
            22.0f),

    MUSIC("Music",
            '\u2669',
            '\u266A',
            '\u266B',
            '\u266C',
            '\u266F',
            Build.VERSION_CODES.LOLLIPOP,
            null,
            32.0f),

    CHESS("Chess",
            '\u265A',
            '\u265B',
            '\u265E',
            '\u265C',
            '\u265F',
            Build.VERSION_CODES.LOLLIPOP,
            null,
            33.0f),

    RECORDER("Recorder",
            '\u23E9',
            '\u23F9',
            '\u23FA',
            '\u23F8',
            '\u23EB',
            Build.VERSION_CODES.O),

    BITS("Bits",
            '\u2596',
            '\u2598',
            '\u259C',
            '\u259E',
            '\u25AA',
            null,
            0.1f,
            32.0f),


    EASY("Easy",
            '\u24D0',
            '\u24B6',
            '\u24EA',
            '\u263A',
            '\u2639',
            null,
            null,
            33.0f),

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
            case PLACEHOLDER: return getSpecialChar(); //TODO migration code
        }
        throw new IllegalStateException("Unknown ObfusChar: " + obfusChar);
    }

    public static Representation valueOfWithDefault(String representationValue) {
        try {
            return Representation.valueOf(representationValue);
        } catch (Exception e) {
            return Representation.DEFAULT_BLOCKS;
        }
    }
}

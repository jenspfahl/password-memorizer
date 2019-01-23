package de.jepfa.obfusser;

public interface Constants {

    String NL = System.getProperty("line.separator");

    int NO_ID = Integer.MIN_VALUE;
    String EMPTY = "";
    int MAX_PASSWD_ATTEMPTS = 3;
    float MAX_PATTERN_DETAIL_DIP = 60.0f;
    int KEY_LENGTH = 512;
    int MIN_PATTERN_LENGTH = 4;
    int MAX_PATTERN_LENGTH = KEY_LENGTH / 8;
}

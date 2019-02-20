package de.jepfa.obfusser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface Constants {

    String NL = System.getProperty("line.separator");
    DateFormat SDF_DT_MEDIUM = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM);
    DateFormat SDF_D_INTERNATIONAL = new SimpleDateFormat("yyyy-MM-dd");

    int NO_ID = Integer.MIN_VALUE;
    String EMPTY = "";
    int MAX_PASSWD_ATTEMPTS = 3;
    float MIN_PATTERN_DETAIL_DIP = 14.0f;
    float MAX_PATTERN_DETAIL_DIP = 60.0f;
    int KEY_LENGTH = 512;
    int MIN_PATTERN_LENGTH = 4;
    int MAX_PATTERN_LENGTH = KEY_LENGTH / 8;
}

package de.jepfa.obfusser.ui.common;

import android.os.Build;
import android.util.TypedValue;
import android.widget.TextView;

import de.jepfa.obfusser.model.Representation;

public class ObfusTextAdjuster {

    public static void adjustText(Representation representation, TextView textView) {
        if (representation.getLetterSpacing() != null) {
            adjustLetterSpacing(representation.getLetterSpacing(), textView);
        }
        if (representation.getTextSize() != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, representation.getTextSize());
        }
    }

    public static void adjustLetterSpacing(float letterSpacing, TextView textView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setLetterSpacing(letterSpacing);
        }
    }
}

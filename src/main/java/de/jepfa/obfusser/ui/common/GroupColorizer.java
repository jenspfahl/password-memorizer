package de.jepfa.obfusser.ui.common;

import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import de.jepfa.obfusser.BuildConfig;
import de.jepfa.obfusser.model.GroupColor;
import de.jepfa.obfusser.model.Group;


public class GroupColorizer {

    public static final char COLOR_INDICATION_FULL = '\u25A0'; // ■
    public static final char COLOR_INDICATION_EMPTY = '\u25A1'; // □
    private static final char COLOR_INDICATION_LIST = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? '\u220e' : '\u25A0'; // ∎ alt: ■

    public static CharSequence getColorizedText( Group group, String text) {
        if (group == null || group.getColor() == 0) {
            return text;
        }
        else {
            return getColorizedText(group.getColor(), text);
        }
    }

    public static SpannableString getColorizedText(int colorInt, String text) {
        return getColorizedSpan(colorInt, text, COLOR_INDICATION_LIST);
    }

    private static SpannableString getColorizedSpan(int colorInt, String text, char indicator) {
        SpannableString span = new SpannableString(String.valueOf(indicator + (text.isEmpty() ? "" : " ") + text));
        int color = GroupColor.getAndroidColor(colorInt);
        span.setSpan(new ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return span;
    }
}

package de.jepfa.obfusser.ui.common;

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.Random;

import de.jepfa.obfusser.model.Group;


public class GroupColorizer {

    private static final char COLOR_INDICATION = '\u220e'; // ∎

    public static CharSequence getColorizedText( Group group, String text) {
        if (group == null || group.getColor() == 0) {
            return text;
        }
        else {
            SpannableString span = new SpannableString(COLOR_INDICATION + (text.isEmpty() ? "" : " ") + text);
            String hexColor = String.format("#%06X", (0xFFFFFF & group.getColor()));
            int color = Color.parseColor(hexColor);
            span.setSpan(new ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            return span;
        }
    }

    public static CharSequence getColorizedButton(Group group) {
        SpannableString span = new SpannableString(String.valueOf(COLOR_INDICATION));
        String hexColor = String.format("#%06X", (0xFFFFFF & group.getColor()));
        int color = Color.parseColor(hexColor);
        span.setSpan(new ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return span;
    }
}

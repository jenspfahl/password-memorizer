package de.jepfa.obfusser.ui.common;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.Representation;

public class LegendShower {
    public static void showLegend(Activity activity, Representation representation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        StringBuilder sb = new StringBuilder();
        buildObfusChar(activity, representation, sb, ObfusChar.LOWER_CASE_CHAR, R.string.lower_char, R.string.lower_char_explanation);
        buildObfusChar(activity, representation, sb, ObfusChar.UPPER_CASE_CHAR, R.string.upper_char, R.string.upper_char_explanation);
        buildObfusChar(activity, representation, sb, ObfusChar.DIGIT, R.string.digit, R.string.digit_explanation);
        buildObfusChar(activity, representation, sb, ObfusChar.SPECIAL_CHAR, R.string.special_char, R.string.special_char_explanation);

        Drawable icon = activity.getApplicationInfo().loadIcon(activity.getPackageManager());
        builder.setTitle(R.string.title_legend)
                .setMessage(sb.toString())
                .setIcon(icon)
                .show();
    }


    private static void buildObfusChar(Activity activity, Representation representation,
                                       StringBuilder sb, ObfusChar obfusChar, int stringId, int explanationId) {
        sb.append(obfusChar.getRepresentation(representation));
        sb.append(" = ");
        sb.append(activity.getString(explanationId));
        sb.append(" (");
        sb.append(activity.getString(stringId));
        sb.append(")");
        sb.append(Constants.NL);
    }
}

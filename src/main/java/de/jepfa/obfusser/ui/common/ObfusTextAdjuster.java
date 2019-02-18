package de.jepfa.obfusser.ui.common;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.model.Representation;

public class ObfusTextAdjuster {

    public static final int DEFAULT_MARGIN = 100;

    public static void adjustTextForRepresentation(Representation representation, TextView textView) {
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

    public static void fitTextSizeToScreen(Activity activity, TextView textView, int margin) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x - (margin * 2); // margin left and right

        Paint paint = textView.getPaint();

        String text = textView.getText().toString();
        float nowWidth = paint.measureText(text);
        float newSize = displayWidth / nowWidth * paint.getTextSize();

        int dp = pxToDp(newSize);
        if (dp < Constants.MAX_PATTERN_DETAIL_DIP) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        }
        else {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Constants.MAX_PATTERN_DETAIL_DIP);
        }
    }


    public static float calcTextSizeToScreenInterpolized(Activity activity, TextView textView, int count, int margin) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x - (margin * 2); // margin left and right

        Paint paint = textView.getPaint();

        String text = textView.getText().toString();
        float nowWidth = paint.measureText(text) * count;
        float newSize = displayWidth / nowWidth * paint.getTextSize();

        int dp = pxToDp(newSize);
        if (dp < Constants.MAX_PATTERN_DETAIL_DIP) {
            return dp;
        }
        else {
            return Constants.MAX_PATTERN_DETAIL_DIP;
        }
    }

    public static int pxToDp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}

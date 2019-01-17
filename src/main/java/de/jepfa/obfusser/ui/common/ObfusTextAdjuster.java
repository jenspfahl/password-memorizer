package de.jepfa.obfusser.ui.common;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.Display;
import android.widget.TextView;

import de.jepfa.obfusser.model.Representation;

public class ObfusTextAdjuster {

    public static final int MARGIN = 100;

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

    public static void fitSizeToScreen(Activity activity, TextView textView) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x - (MARGIN * 2); // margin left and right

        Paint paint = textView.getPaint();

        String text = textView.getText().toString();
        float nowWidth = paint.measureText(text);
        float newSize = displayWidth / nowWidth * paint.getTextSize();
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);

    }
}

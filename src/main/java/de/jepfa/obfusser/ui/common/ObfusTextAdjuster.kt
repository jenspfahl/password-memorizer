package de.jepfa.obfusser.ui.common

import android.app.Activity
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.widget.TextView

import de.jepfa.obfusser.Constants
import de.jepfa.obfusser.model.Representation

object ObfusTextAdjuster {

    val DEFAULT_MARGIN = 100
    val EXTRA_MARGIN = 200

    fun adjustTextForRepresentation(representation: Representation, textView: TextView) {
        if (representation.letterSpacing != null) {
            adjustLetterSpacing(representation.letterSpacing!!, textView)
        }
        if (representation.textSize != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, representation.textSize!!)
        }
    }

    fun adjustLetterSpacing(letterSpacing: Float, textView: TextView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.letterSpacing = letterSpacing
        }
    }


    fun calcTextSizeToScreen(activity: Activity, textView: TextView, string: String, margin: Int): Float {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val displayWidth = size.x - margin * 2 // margin left and right

        val paint = textView.paint

        val nowWidth = paint.measureText(string)
        val newSize = displayWidth / nowWidth * paint.textSize

        val dp = pxToDp(newSize)
        return if (dp < Constants.MIN_PATTERN_DETAIL_DIP) {
            Constants.MIN_PATTERN_DETAIL_DIP
        } else if (dp < Constants.MAX_PATTERN_DETAIL_DIP) {
            dp.toFloat()
        } else {
            Constants.MAX_PATTERN_DETAIL_DIP
        }
    }

    fun pxToDp(px: Float): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }
}

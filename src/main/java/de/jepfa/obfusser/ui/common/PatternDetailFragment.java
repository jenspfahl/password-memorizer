package de.jepfa.obfusser.ui.common;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.model.Representation;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;

public abstract class PatternDetailFragment extends SecureFragment {

    public interface HintUpdateListener {
        void onHintUpdated(int index);
    }

    public static final String ARG_MODE = "mode";
    public static final int SHOW_DETAIL = 1;
    public static final int SELECT_HINTS = 2;

    protected int mode;

    private HintUpdateListener hintUpdateListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getArguments().getInt(ARG_MODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(getFragmentDetailId(), container, false);
        SecurePatternHolder pattern = getPattern();

        if (pattern != null) {
            final TextView obfusTextView = rootView.findViewById(getTextViewId());
            ObfusTextAdjuster.adjustText(getSecureActivity().getPatternRepresentation(), obfusTextView);

            switch (mode) {
                case SELECT_HINTS:
                    onCreateForNewPatternSelectHints(pattern, obfusTextView);
                    break;
                case SHOW_DETAIL:
                    onCreateForShowPatternDetails(pattern, obfusTextView);
                    break;
            }

            //TODO fitSizeToScreen(obfusTextView);
        }

        return rootView;
    }

    protected abstract int getFragmentDetailId();

    protected abstract int getTextViewId();

    protected abstract SecurePatternHolder getPattern();

    protected abstract String getFinalPatternForDetails(SecurePatternHolder pattern, int counter);

    protected abstract String getPatternRepresentationForDetails(SecurePatternHolder pattern);

    public void setHintUpdateListener(HintUpdateListener hintUpdateListener) {
        this.hintUpdateListener = hintUpdateListener;
    }

    private void onCreateForShowPatternDetails(final SecurePatternHolder pattern, final TextView obfusTextView) {
        String patternString = buildPatternString(
                pattern,
                getPatternRepresentationForDetails(pattern),
                false);
        SpannableString span = getSpannableString(pattern, patternString, pattern.getInfo());

        obfusTextView.setText(span, TextView.BufferType.NORMAL);

        final AtomicInteger clickCounter = new AtomicInteger(1);
        obfusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int counter = clickCounter.getAndIncrement();

                String finalPatternString = getFinalPatternForDetails(pattern, counter);

                SpannableString span = getSpannableString(pattern, finalPatternString, pattern.getInfo());
                obfusTextView.setText(span, TextView.BufferType.NORMAL);
            }

        });

        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getActivity(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float size = obfusTextView.getTextSize() * detector.getScaleFactor();
                obfusTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                return true;
            }

        });

        obfusTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getPointerCount() == 2) {
                    return scaleGestureDetector.onTouchEvent(motionEvent);
                }
                else {
                    return false;
                }
            }
        });
    }

    private void onCreateForNewPatternSelectHints(final SecurePatternHolder pattern, final TextView obfusTextView) {
        String patternString = pattern.getPatternRepresentationWithNumberedPlaceholder(
                SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                getSecureActivity().getPatternRepresentation());
        final SpannableStringBuilder span = new SpannableStringBuilder(patternString);

        for (int i = 0; i < patternString.length(); i++) {

            final boolean fenabled = pattern.hasHint(i);

            final int fi = i;
            ClickableSpan clickSpan = new ClickableSpan() {

                private boolean enabled = fenabled;
                private int index = fi;

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    int color;
                    if (enabled) {
                        color = getResources().getColor(R.color.colorAccent);

                    }
                    else {
                        color = Color.BLACK;
                    }
                    ds.setColor(color);
                    ds.setUnderlineText(false); // overwrite super underline setting
                }

                @Override
                public void onClick(View yourTextView) {

                    if (!enabled && pattern.getHints().size() == NumberedPlaceholder.values().length) {
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.title_set_revealed)
                                .setMessage(R.string.message_set_revealed)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();
                        return;
                    }

                    enabled = !enabled;
                    if (enabled) {
                        pattern.addPotentialHint(index);

                        if (hintUpdateListener != null) {
                            hintUpdateListener.onHintUpdated(index);
                        }
                    }
                    else {

                        if (pattern.isFilledHint(index)) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(R.string.title_delete_revealed_character)
                                    .setMessage(getString(R.string.message_delete_revealed_character,
                                            pattern.getNumberedPlaceholder(index).toRepresentation()))
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {

                                            pattern.removePotentialHint(index);

                                            if (hintUpdateListener != null) {
                                                hintUpdateListener.onHintUpdated(index);
                                            }
                                            onCreateForNewPatternSelectHints(pattern, obfusTextView);

                                        }})
                                    .setNegativeButton(android.R.string.no, null)
                                    .show();
                        }
                        else {
                            pattern.removePotentialHint(index);

                            if (hintUpdateListener != null) {
                                hintUpdateListener.onHintUpdated(index);
                            }
                        }

                    }

                    onCreateForNewPatternSelectHints(pattern, obfusTextView);
                }
            };
            span.setSpan(clickSpan, i, i + 1, 0);

        }

        obfusTextView.setText(span);
        obfusTextView.setMovementMethod(LinkMovementMethod.getInstance());
        ObfusTextAdjuster.adjustText(getSecureActivity().getPatternRepresentation(), obfusTextView);
    }

    @NonNull
    protected String buildPatternString(SecurePatternHolder pattern, String patternString, boolean withHints) {
        StringBuilder sb = new StringBuilder(patternString);
        if (withHints && pattern.getHints() != null && !pattern.getHints().isEmpty()) {
            //TODO move this in anotherUI component, so pattern can be fit automatically to the screen size w/o hints
            sb.append(System.lineSeparator());
            int counter = 0;
            for (String hint : pattern.getHints(SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity())).values()) {
                counter++;
                sb.append(System.lineSeparator());
                sb.append(NumberedPlaceholder.fromPlaceholderNumber(counter).toRepresentation());
                sb.append("=");
                if (hint == null || hint.isEmpty()) {
                    hint = getString(R.string.hint_tbd);
                }
                sb.append(hint);
            }
        }
        return sb.toString();
    }


    @NonNull
    private SpannableString getSpannableString(SecurePatternHolder pattern, String patternString, String prefixText) {
        SpannableString span;
        int start = 0;
        if (TextUtils.isEmpty(prefixText)) {
            span = new SpannableString(patternString);
        }
        else {
            span = new SpannableString(prefixText + Constants.NL + patternString);
            start = prefixText.length() + 1;
        }

        for (int i = 0; i < pattern.getPatternLength(); i++) {
            int j = i + 1;
            String hint = pattern.getHint(i, SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()));
            if (hint != null) {
                span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start + i, start + j, Spanned.SPAN_MARK_MARK);
            }

        }

        for (int i = pattern.getPatternLength(); i < patternString.length(); i++) {
            int j = i + 1;
            span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start + i, start + j, Spanned.SPAN_MARK_MARK);

        }
        return span;
    }


    private void fitSizeToScreen(TextView textView) {
        Display display = getActivity().getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x - 10;

        Paint paint = new Paint();
        Rect bounds = new Rect();

        paint.setTypeface(textView.getTypeface());
        float textSize = textView.getTextSize();
        paint.setTextSize(textSize);
        String text = textView.getText().toString();
        paint.getTextBounds(text, 0, text.length(), bounds);

        while (bounds.width() > displayWidth) {
            textSize--;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }
//TODO
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(textSize, displayWidth));
       // textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
    }

    @Override
    public void refresh() {
        getActivity().recreate(); //TODO
    }




}

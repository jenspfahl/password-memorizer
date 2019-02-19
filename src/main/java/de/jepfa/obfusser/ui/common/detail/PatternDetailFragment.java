package de.jepfa.obfusser.ui.common.detail;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.ui.common.Debug;
import de.jepfa.obfusser.ui.common.ObfusTextAdjuster;

public abstract class PatternDetailFragment extends SecureFragment {

    public static final String CURRENT_CLICK_STEP = "current_click_step";
    public static final int DEFAULT_CLICK_STEP = 0;

    private TextView hintsTextView;
    private TextView infoTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pattern_detail, container, false);
        SecurePatternHolder pattern = getPattern();

        if (pattern != null) {
            final TextView obfusTextView = rootView.findViewById(R.id.pattern_detail_obfuschar);
            ObfusTextAdjuster.adjustTextForRepresentation(getSecureActivity().getPatternRepresentation(), obfusTextView);

            infoTextView = rootView.findViewById(R.id.pattern_info_textview);
            hintsTextView = rootView.findViewById(R.id.pattern_hints_textview);

            onCreateForShowPatternDetails(pattern, obfusTextView);

        }

        return rootView;
    }

    protected abstract SecurePatternHolder getPattern();

    protected abstract String getFinalPatternForDetails(SecurePatternHolder pattern, int counter);

    protected abstract boolean showHints(int counter);


    private void onCreateForShowPatternDetails(final SecurePatternHolder pattern, final TextView obfusTextView) {
        if (pattern.getInfo() != null) {
            infoTextView.setText(pattern.getInfo());
        }

        int initClickStep = getArguments().getInt(CURRENT_CLICK_STEP, DEFAULT_CLICK_STEP);

        String patternString = getFinalPatternForDetails(pattern, initClickStep);

        if (showHints(initClickStep)) {
            hintsTextView.setText(buildHintsString(pattern));
        }

        SpannableString span = getSpannableString(pattern, patternString);

        obfusTextView.setText(span, TextView.BufferType.NORMAL);
        float estimatedSize = ObfusTextAdjuster.calcTextSizeToScreen(getActivity(), obfusTextView,
                obfusTextView.getText().toString(), ObfusTextAdjuster.DEFAULT_MARGIN);
        obfusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, estimatedSize);

        final AtomicInteger clickCounter = new AtomicInteger(initClickStep);
        obfusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int counter = clickCounter.incrementAndGet();
                getArguments().putInt(CURRENT_CLICK_STEP, clickCounter.get());

                String finalPatternString = getFinalPatternForDetails(pattern, counter);
                if (showHints(counter)) {
                    hintsTextView.setText(buildHintsString(pattern));
                }
                else {
                    hintsTextView.setText(null);
                }

                SpannableString span = getSpannableString(pattern, finalPatternString);
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

        final GestureDetector longPressGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent event) {
                String message = pattern.toString();
                byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
                if (secret != null) {
                    message = message + Constants.NL
                            + "uuidkey="
                            + Debug.INSTANCE.endOfArrayToString(
                                    pattern.getUUIDKey(secret, SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity())), 4);
                }
                new AlertDialog.Builder(getActivity())
                            .setTitle("Debug pattern")
                            .setMessage(message)
                            .show();
            }
        });


        obfusTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getPointerCount() == 2) {
                    return scaleGestureDetector.onTouchEvent(motionEvent);
                }
                else {
                    if (Debug.INSTANCE.isDebug()) {
                        return longPressGestureDetector.onTouchEvent(motionEvent);
                    }
                    return false;//obfusTextView.performClick();
                }
            }
        });
    }


    protected String buildHintsString(SecurePatternHolder pattern) {
        StringBuilder sb = new StringBuilder();
        byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
        final boolean withUuid = SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity());
        if (pattern.getHintsCount() > 0) {
            int counter = 0;
            for (String hint : pattern.getHints(secret, withUuid).values()) {
                counter++;
                sb.append(System.lineSeparator());
                sb.append(NumberedPlaceholder.fromPlaceholderNumber(counter).toRepresentation());
                sb.append("=");
                if (hint == null || hint.isEmpty()) {
                    hint = getString(R.string.hint_tbd);
                }
                sb.append(hint);
            }
            return sb.toString();
        }
        else {
            return getString(R.string.nothing_revealed);
        }

    }


    @NonNull
    private SpannableString getSpannableString(SecurePatternHolder pattern, String patternString) {
        SpannableString span = new SpannableString(patternString);
        byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
        boolean withUuid = SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity());

        for (int i = 0; i < pattern.getPatternLength(); i++) {
            int j = i + 1;
            String hint = pattern.getHint(i, secret, withUuid);
            if (hint != null) {
                span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), i, j, Spanned.SPAN_MARK_MARK);
            }

        }

        for (int i = pattern.getPatternLength(); i < patternString.length(); i++) {
            int j = i + 1;
            span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), i, j, Spanned.SPAN_MARK_MARK);

        }
        return span;
    }


    @Override
    public void refresh() {
        getActivity().recreate(); //TODO
    }

}

package de.jepfa.obfusser.ui.common.input;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.ObfusTextAdjuster;

public class PatternSelectHintsAdapter extends RecyclerView.Adapter<PatternSelectHintsAdapter.ViewHolder> {

    private final SecurePatternHolder pattern;
    private final SecureActivity activity;
    private HintUpdateListener hintUpdateListener;
    private Float estimatedSize;

    public PatternSelectHintsAdapter(SecurePatternHolder pattern, SecureActivity activity,
                                     HintUpdateListener hintUpdateListener) {
        this.pattern = pattern;
        this.activity = activity;
        this.hintUpdateListener = hintUpdateListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pattern_select_hint, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(activity);

        boolean encWithUuid = SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity);
        final String patternString = pattern.getPatternRepresentation(
                secret,
                activity.getPatternRepresentation(),
                encWithUuid);
        final String patternStringNumbered = pattern.getPatternRepresentationWithNumberedPlaceholder(
                secret,
                activity.getPatternRepresentation(),
                encWithUuid);

        final boolean hasHint = pattern.hasHint(position, secret, encWithUuid);

        if (hasHint) {
            holder.selectHintTextView.setText(pattern.getNumberedPlaceholder(position, secret, encWithUuid).toRepresentation());
            holder.selectHintTextView.setTextColor(activity.getResources().getColor(R.color.colorAccent));
        }
        else {
            holder.selectHintTextView.setText(String.valueOf(patternString.charAt(position)));
            holder.selectHintTextView.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
        }

        holder.selectHintTextView.setBackgroundColor(activity.getResources().getColor(R.color.colorLightBG));

        holder.selectHintTextView.setOnClickListener(new View.OnClickListener() {

            private boolean enabled = hasHint;
            private int index = position;

            @Override
            public void onClick(View v) {

                if (!enabled && pattern.getHintsCount() == NumberedPlaceholder.count()) {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.title_set_revealed)
                            .setMessage(R.string.message_set_revealed)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                    return;
                }

                final byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(activity);
                final boolean withUuid = SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity);

                enabled = !enabled;
                if (enabled) {
                    pattern.addHint(index, secret, withUuid);

                    holder.selectHintTextView.setTextColor(activity.getResources().getColor(R.color.colorAccent));
                    holder.selectHintTextView.setText(pattern.getNumberedPlaceholder(index, secret, withUuid).toRepresentation());

                    if (hintUpdateListener != null) {
                        hintUpdateListener.onHintUpdated(index);
                    }
                }
                else {

                    if (pattern.isFilledHint(index, secret, withUuid)) {
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.title_delete_revealed_character)
                                .setMessage(activity.getString(R.string.message_delete_revealed_character,
                                        pattern.getNumberedPlaceholder(index, secret, withUuid).toRepresentation()))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        pattern.removeHint(index, secret, withUuid);

                                        holder.selectHintTextView.setTextColor(Color.BLACK);
                                        holder.selectHintTextView.setText(String.valueOf(patternString.charAt(position)));

                                        if (hintUpdateListener != null) {
                                            hintUpdateListener.onHintUpdated(index);
                                        }

                                        updateAll();

                                    }})
                                .setNegativeButton(android.R.string.no, null)
                                .show();
                    }
                    else {
                        pattern.removeHint(index, secret, withUuid);

                        holder.selectHintTextView.setTextColor(Color.BLACK);
                        holder.selectHintTextView.setText(String.valueOf(patternString.charAt(position)));

                        if (hintUpdateListener != null) {
                            hintUpdateListener.onHintUpdated(index);
                        }
                    }
                }

                updateAll();
            }
        });

        if (estimatedSize == null) {
           estimatedSize = ObfusTextAdjuster.INSTANCE.calcTextSizeToScreen(activity, holder.selectHintTextView, patternStringNumbered,
                   ObfusTextAdjuster.INSTANCE.getDEFAULT_MARGIN());
        }
        holder.selectHintTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, estimatedSize);

    }

    private void updateAll() {
        estimatedSize = null;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return pattern.getPatternLength();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView selectHintTextView;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.selectHintTextView = view.findViewById(R.id.select_hint_textview);
        }
    }
}

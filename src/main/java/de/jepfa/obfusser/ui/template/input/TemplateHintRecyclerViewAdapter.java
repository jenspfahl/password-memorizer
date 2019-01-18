package de.jepfa.obfusser.ui.template.input;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;

public class TemplateHintRecyclerViewAdapter extends RecyclerView.Adapter<TemplateHintRecyclerViewAdapter.ViewHolder> {

    private final Template template;
    private final SecureActivity activity;

    public TemplateHintRecyclerViewAdapter(Template template, SecureActivity activity) {
        this.template = template;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.template_hint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.hintData = template.getHintDataByPosition(position, SecureActivity.SecretChecker.getOrAskForSecret(activity));
        NumberedPlaceholder numberedPlaceholder = NumberedPlaceholder.fromPlaceholderNumber(position + 1);
        holder.placeholder.setText(numberedPlaceholder.toRepresentation());
        holder.hint.setText(holder.hintData.second != null ? holder.hintData.second : "");

        if (holder.hint instanceof EditText) {
            EditText editText = (EditText) holder.hint;

            // update model
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    template.setHint(
                            holder.hintData.first,
                            holder.hint.getText().toString(),
                            SecureActivity.SecretChecker.getOrAskForSecret(activity));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return template.getHintsCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView placeholder;
        public final TextView hint;
        public Pair<Integer,String> hintData;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            placeholder = view.findViewById(R.id.placeholder_text);
            hint = view.findViewById(R.id.hint_text);
        }

        public void setError(String error) {
            hint.setError(error);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + hint.getText() + "'";
        }
    }
}

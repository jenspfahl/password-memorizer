package de.jepfa.obfusser.ui.template.list;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.ui.template.detail.TemplateDetailActivity;
import de.jepfa.obfusser.util.IntentUtil;

public class TemplateListAdapter extends RecyclerView.Adapter<TemplateListAdapter.ViewHolder> {

    private final View.OnClickListener listener;
    private final SecureActivity activity;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView patternView;
        final ImageView iconView;

        ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.template_list_name);
            patternView = view.findViewById(R.id.template_list_pattern);
            iconView = view.findViewById(R.id.template_list_menu_popup);

            iconView.setOnClickListener(listener);
        }
    }

    private final LayoutInflater inflater;
    private List<Template> templates; // Cached copy of templates
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Template item = (Template) view.getTag();
            Context context = view.getContext();
            Intent intent = new Intent(context, TemplateDetailActivity.class);
            IntentUtil.setTemplateExtra(intent, item);
            context.startActivity(intent);
        }
    };

    public TemplateListAdapter(View.OnClickListener listener, Context context, SecureActivity activity) {
        inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.template_list_content,
                parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (templates != null || !templates.isEmpty()) {
            holder.nameView.setText(templates.get(position).getName());

            boolean showPattern = PreferenceManager
                    .getDefaultSharedPreferences(activity)
                    .getBoolean(SettingsActivity.PREF_SHOW_PATTERN_IN_OVERVIEW, true);

            if (showPattern) {
                holder.patternView.setText(
                        templates.get(position).getPatternRepresentationWithNumberedPlaceholder(
                                SecureActivity.SecretChecker.getOrAskForSecret(activity),
                                activity.getPatternRepresentation()
                        ));
            }
            else {
                holder.patternView.setText(
                        templates.get(position).getHiddenPatternRepresentation(
                                activity.getPatternRepresentation()
                        ));
            }

            holder.iconView.setTag(templates.get(position));
            holder.nameView.setOnClickListener(onClickListener);
            holder.nameView.setTag(templates.get(position));
            holder.patternView.setOnClickListener(onClickListener);
            holder.patternView.setTag(templates.get(position));
        }
    }

    void setTemplates(List<Template> templates){
        this.templates = templates;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (templates != null)
            return templates.size();
        else return 0;
    }
}
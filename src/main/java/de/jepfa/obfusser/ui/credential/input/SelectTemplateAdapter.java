package de.jepfa.obfusser.ui.credential.input;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;

public class SelectTemplateAdapter extends BaseAdapter {

    private final SecureActivity activity;
    private List<Template> templates;
    private Context context;

    public SelectTemplateAdapter(Context context, SecureActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        if (templates != null) {
            return templates.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return templates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return templates.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(context);
        Template template = templates.get(position);
        textView.setText(template.getName() +
                " " + template.getPatternRepresentationWithNumberedPlaceholder(
                SecureActivity.SecretChecker.getOrAskForSecret(activity)
        ));
        return textView;
    }

    public void setTemplates(List<Template> templates){
        this.templates = templates;
        notifyDataSetChanged();
    }
}

package de.jepfa.obfusser.ui.group.assignment;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.jepfa.obfusser.model.Group;

public class SelectGroupAdapter extends BaseAdapter {
    public List<Group> groups;
    private Context context;

    public SelectGroupAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (groups != null) {
            return groups.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return groups.get(position).getId();
    }

    public Integer getPositionForGroupId(int groupId) {
        if (getCount() == 0) {
            return null;
        }
        int position = 0;
        for (Group group : groups) {
            if (group.getId() == groupId) {
                return position;
            }
            position++;
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(context);
        Group group = groups.get(position);
        textView.setText(group.getName());
        return textView;
    }

    public void setGroups(List<Group> groups){
        this.groups = groups;
        notifyDataSetChanged();
    }
}

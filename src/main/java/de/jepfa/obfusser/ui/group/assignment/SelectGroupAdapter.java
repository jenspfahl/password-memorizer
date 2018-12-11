package de.jepfa.obfusser.ui.group.assignment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Group;

public class SelectGroupAdapter extends RecyclerView.Adapter<SelectGroupAdapter.ViewHolder> {


    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView nameView;

        ViewHolder(View view) {
            super(view);
            iconView = view.findViewById(R.id.group_selection_indicator);
            nameView = view.findViewById(R.id.group_selection_name);
        }
    }

    private final LayoutInflater inflater;
    private Map<Integer, Boolean> selectedMap = new HashMap<>();
    private Integer selectedGroupId;
    private List<Group> groups; // Cached copy of groups

    public SelectGroupAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public SelectGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.group_selection_item,
                parent, false);
        return new SelectGroupAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SelectGroupAdapter.ViewHolder holder, int position) {
        if (groups != null || !groups.isEmpty()) {
            holder.nameView.setText(groups.get(position).getName());
            holder.iconView.setTag(groups.get(position));
            holder.nameView.setTag(groups.get(position));

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view1) {
                    Group group = (Group) view1.getTag();
                    Integer pos = getPositionForGroupId(group.getId());
                    if (!selectedMap.containsKey(pos)) {
                        selectedMap.put(pos, false);
                    }

                    boolean isCurrentSelected = selectedMap.get(pos);
                    if (isCurrentSelected) {
                        selectedGroupId = null;
                        holder.iconView.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);
                        selectedMap.put(pos, false);
                    } else {
                        selectedGroupId = group.getId();
                        holder.iconView.setImageResource(R.drawable.ic_radio_button_checked_black_24dp);
                        selectedMap.put(pos, true);

                        //TODO unselect old selection
                    }
                }
            };
            holder.nameView.setOnClickListener(clickListener);
            holder.iconView.setOnClickListener(clickListener);

        }
    }

    void setGroupsAndSelection(List<Group> groups, Integer selectedGroupId){
        this.groups = groups;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (groups != null)
            return groups.size();
        else return 0;
    }

    public Integer getSelectedGroupId() {
        return selectedGroupId;
    }

    private Integer getPositionForGroupId(int groupId) {
        if (getItemCount() == 0) {
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

}
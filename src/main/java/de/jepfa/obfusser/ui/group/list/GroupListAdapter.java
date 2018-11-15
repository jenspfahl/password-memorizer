package de.jepfa.obfusser.ui.group.list;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.group.detail.GroupDetailActivity;
import de.jepfa.obfusser.util.IntentUtil;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private final View.OnClickListener listener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final ImageView iconView;

        ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.group_list_name);
            iconView = view.findViewById(R.id.group_list_menu_popup);

            iconView.setOnClickListener(listener);
        }
    }

    private final LayoutInflater inflater;
    private List<Group> groups; // Cached copy of groups
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Group item = (Group) view.getTag();
            Context context = view.getContext();
            Intent intent = new Intent(context, GroupDetailActivity.class);
            IntentUtil.setGroupExtra(intent, item);
            context.startActivity(intent);
        }
    };

    public GroupListAdapter(View.OnClickListener listener, Context context) {
        inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.group_list_content,
                parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (groups != null || !groups.isEmpty()) {
            holder.nameView.setText(groups.get(position).getName());

            holder.iconView.setTag(groups.get(position));
            holder.nameView.setOnClickListener(onClickListener);
            holder.nameView.setTag(groups.get(position));
        }
    }

    void setGroups(List<Group> groups){
        this.groups = groups;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (groups != null)
            return groups.size();
        else return 0;
    }
}
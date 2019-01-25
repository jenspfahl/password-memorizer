package de.jepfa.obfusser.ui.credential.list;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.credential.detail.CredentialDetailActivity;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.IntentUtil;

public class CredentialExpandableListAdapter extends BaseExpandableListAdapter implements Filterable {

    private final CredentialListFragmentBase fragment;
    private final LayoutInflater inflater;
    private final ExpandableListView listView;
    private Map<Integer, List<Credential>> groupIdCredentials;
    private List<Group> groups;
    private Map<Integer, List<Credential>> originGroupIdCredentials;
    private List<Group> originGroups;

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filterResults.values = originGroupIdCredentials;
                } else {
                    Map<Integer, List<Credential>> filteredMap = new HashMap<>();
                    for (Map.Entry<Integer, List<Credential>> entry : originGroupIdCredentials.entrySet()) {
                        int groupId = entry.getKey();
                        List<Credential> credentials = entry.getValue();
                        for (Credential credential : credentials) {
                            if (credential.getName().toLowerCase().contains(charString.toLowerCase())) {
                                if (!filteredMap.containsKey(groupId)) {
                                    filteredMap.put(groupId, new ArrayList<Credential>());
                                }
                                Group group = findGroupById(groups, groupId);
                                if (group != null) {
                                    int position = findPositionByGroup(groups, group);
                                    listView.expandGroup(position);
                                }
                                filteredMap.get(groupId).add(credential);
                            }
                        }

                    }

                    filterResults.values = filteredMap;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                groupIdCredentials = (Map<Integer, List<Credential>>) filterResults.values;

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Credential item = (Credential) view.getTag();
            Context context = view.getContext();
            Intent intent = new Intent(context, CredentialDetailActivity.class);
            IntentUtil.setCredentialExtra(intent, item);
            context.startActivity(intent);
        }
    };


    CredentialExpandableListAdapter(CredentialListFragmentBase fragment, ExpandableListView listView ) {
        inflater = LayoutInflater.from(fragment.getContext());
        this.fragment = fragment;
        this.listView = listView;
    }

    void setCredentials(List<Group> allGroups, List<Credential> credentials) {
        groups = new ArrayList<>(allGroups.size());
        groupIdCredentials = new HashMap<>();
        originGroups = groups;
        originGroupIdCredentials = groupIdCredentials;


        for (Credential credential : credentials) {
            int groupId;
            if (credential.getGroupId() != null) {
                groupId = credential.getGroupId();
            }
            else {
                groupId = Constants.NO_ID;
            }
            if (!groupIdCredentials.containsKey(groupId)) {
                groupIdCredentials.put(groupId, new ArrayList<Credential>());
                Group assocGroup = findGroupById(allGroups, groupId);
                if (assocGroup != null) {
                    groups.add(assocGroup);
                }
                else {
                    Group noGroupGroup = new Group();
                    noGroupGroup.setId(Constants.NO_ID);
                    noGroupGroup.setName(fragment.getString(R.string.no_group));
                    groups.add(0, noGroupGroup);
                }
            }
            List<Credential> credentialsForGroup = groupIdCredentials.get(groupId);
            credentialsForGroup.add(credential);
        }

        notifyDataSetChanged();
    }


    @Override
    public int getGroupCount() {
        if (groups != null)
            return groups.size();
        else return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupIdCredentials != null) {
            Group group = (Group) getGroup(groupPosition);
            if (group != null) {
                List<Credential> credentials = groupIdCredentials.get(group.getId());
                if (credentials != null) {
                    return credentials.size();
                }
            }
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (groups != null) {
            return groups.get(groupPosition);
        }
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Group group = (Group) getGroup(groupPosition);
        if (group != null) {
            List<Credential> credentials = groupIdCredentials.get(group.getId());
            if (credentials != null) {
                return credentials.get(childPosition);
            }
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        Group group = (Group) getGroup(groupPosition);
        if (group != null) {
            return group.getId();
        }
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Credential credential = (Credential) getChild(groupPosition, childPosition);
        if (credential != null) {
            return credential.getId();
        }
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Group group = (Group) getGroup(groupPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_expand_content,
                    parent, false);
        }
        TextView nameView = convertView.findViewById(R.id.group_expand_title);
        nameView.setText(group.getName());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Credential credential = (Credential) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.credential_flat_list_content,
                    parent, false);
        }

        TextView nameView = convertView.findViewById(R.id.credential_list_name);
        TextView patternView = convertView.findViewById(R.id.credential_list_pattern);
        ImageView iconView = convertView.findViewById(R.id.credential_list_menu_popup);

        nameView.setText(credential.getName());

        boolean showPattern = PreferenceManager
                .getDefaultSharedPreferences(fragment.getActivity())
                .getBoolean(SettingsActivity.PREF_SHOW_PATTERN_IN_OVERVIEW, true);

        if (showPattern) {
            patternView.setText(credential.getPatternRepresentationHinted(
                    SecureActivity.SecretChecker.getOrAskForSecret(fragment.getSecureActivity()),
                    fragment.getSecureActivity().getPatternRepresentation(),
                    SecureActivity.SecretChecker.isEncWithUUIDEnabled(fragment.getSecureActivity())));
        }
        else {
            patternView.setText(credential.getHiddenPatternRepresentation(
                    fragment.getSecureActivity().getPatternRepresentation()));
        }

        iconView.setTag(credential);
        nameView.setOnClickListener(onClickListener);
        nameView.setTag(credential);
        patternView.setOnClickListener(onClickListener);
        patternView.setTag(credential);
        iconView.setOnClickListener(fragment);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private Group findGroupById(List<Group> groups, int groupId) {
        for (Group group : groups) {
            if (group.getId() == groupId) {
                return group;
            }
        }
        return null;
    }
    private int findPositionByGroup(List<Group> groups, Group group) {
        int i = 0;
        for (Group g : groups) {
            if (g == group) {
                return i;
            }
            i++;
        }
        return i;
    }
}
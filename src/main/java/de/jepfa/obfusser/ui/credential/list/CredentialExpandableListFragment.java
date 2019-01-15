package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.Observer;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Filterable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.settings.SettingsActivity;


public class CredentialExpandableListFragment extends CredentialListFragmentBase {

    private CredentialExpandableListAdapter expandableAdapter;


    @Override
    protected int getViewId() {
        return R.layout.navtab_credential_expandable_list;
    }

    @Override
    protected Filterable getFilterable() {
        return expandableAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        final ExpandableListView listView = view.findViewById(R.id.credential_expandable_list);
        assert listView != null;

        expandableAdapter = new CredentialExpandableListAdapter(this, listView);
        listView.setAdapter(expandableAdapter);

        final SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this.getActivity());

        credentialListViewModel
                .getRepo()
                .getAllCredentialsSortByGroupAndName()
                .observe(this, new Observer<List<Credential>>() {
                    @Override
                    public void onChanged(@Nullable final List<Credential> credentials) {
                        groupListViewModel
                                .getRepo()
                                .getAllGroupsSortByName()
                                .observe(CredentialExpandableListFragment.this, new Observer<List<Group>>() {
                                    @Override
                                    public void onChanged(@Nullable final List<Group> groups) { //TODO find better way instead of nested observe call
                                        expandableAdapter.setCredentials(groups, credentials);
                                        expandStoredGroups(listView, defaultSharedPreferences);
                                    }
                                });
                    }
                });



        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Set<String> expandedGroups = defaultSharedPreferences
                        .getStringSet(SettingsActivity.PREF_EXPANDED_GROUPS, new HashSet<String>());

                expandedGroups.add(String.valueOf(groupPosition));

                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                editor.putStringSet(SettingsActivity.PREF_EXPANDED_GROUPS, expandedGroups);
                editor.commit();
            }
        });

        listView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                Set<String> expandedGroups = defaultSharedPreferences
                        .getStringSet(SettingsActivity.PREF_EXPANDED_GROUPS, new HashSet<String>());

                expandedGroups.remove(String.valueOf(groupPosition));

                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                editor.putStringSet(SettingsActivity.PREF_EXPANDED_GROUPS, expandedGroups);
                editor.commit();
            }
        });

        return view;
    }

    private void expandStoredGroups(ExpandableListView listView, SharedPreferences defaultSharedPreferences) {
        if (listView != null) {
            Set<String> expandedGroups = defaultSharedPreferences
                    .getStringSet(SettingsActivity.PREF_EXPANDED_GROUPS, new HashSet<String>());

            int count = expandableAdapter.getGroupCount();
            for (int position = 0; position < count; position++) {
                if (expandedGroups.contains(String.valueOf(position))) {
                    listView.expandGroup(position);
                }
            }
        }
    }

    @Override
    public void refresh() {
        expandableAdapter.notifyDataSetChanged();
    }
}
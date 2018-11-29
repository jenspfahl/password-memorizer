package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;


public class CredentialExpandableListFragment extends CredentialListFragmentBase {

    private CredentialExpandableListAdapter expandableAdapter;


    @Override
    protected int getViewId() {
        return R.layout.navtab_credential_expandable_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        final ExpandableListView listView = view.findViewById(R.id.credential_expandable_list);
        assert listView != null;

        expandableAdapter = new CredentialExpandableListAdapter(this);
        listView.setAdapter(expandableAdapter);


        //TODO wrong rendering
        groupListViewModel
                .getRepo()
                .getAllGroupsSortByName()
                .observe(this, new Observer<List<Group>>() {
                    @Override
                    public void onChanged(@Nullable final List<Group> groups) {
                        expandableAdapter.setGroups(groups);
                        expandAllGroups(listView);
                    }
                });

        credentialListViewModel
                .getRepo()
                .getAllCredentialsSortByGroupAndName()
                .observe(this, new Observer<List<Credential>>() {
                    @Override
                    public void onChanged(@Nullable final List<Credential> credentials) {
                        expandableAdapter.setCredentials(credentials);
                        expandAllGroups(listView);
                    }
                });

        return view;
    }

    private void expandAllGroups(ExpandableListView listView) {
        if (listView != null) {
            int count = expandableAdapter.getGroupCount();
            for (int position = 1; position <= count; position++)
                listView.expandGroup(position - 1);
        }
    }

    @Override
    public void refresh() {
        expandableAdapter.notifyDataSetChanged();
    }
}
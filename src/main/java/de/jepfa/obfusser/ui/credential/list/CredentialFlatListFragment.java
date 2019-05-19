package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;


public class CredentialFlatListFragment extends CredentialListFragmentBase {

    private CredentialFlatListAdapter listAdapter;
    private RecyclerView recyclerView;


    @Override
    protected int getViewId() {
        return R.layout.navtab_credential_flat_list;
    }

    @Override
    protected Filterable getFilterable() {
        return listAdapter;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        recyclerView = view.findViewById(R.id.credential_flat_list);
        assert recyclerView != null;

        listAdapter = new CredentialFlatListAdapter(this);
        recyclerView.setAdapter(listAdapter);

        credentialListViewModel
                .getCredentials()
                .observe(this, new Observer<List<Credential>>() {
                    @Override
                    public void onChanged(@Nullable final List<Credential> credentials) {
                        groupListViewModel
                                .getGroups()
                                .observe(CredentialFlatListFragment.this, new Observer<List<Group>>() {
                                    @Override
                                    public void onChanged(@Nullable final List<Group> groups) { //TODO find better way instead of nested observe call
                                        listAdapter.setGroupsAndCredentials(groups, credentials);
                                    }
                                });
                    }
                });

        return view;
    }

    @Override
    public void refresh() {
        refreshMenuLockItem();
        recyclerView.post(new Runnable()
        {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

}

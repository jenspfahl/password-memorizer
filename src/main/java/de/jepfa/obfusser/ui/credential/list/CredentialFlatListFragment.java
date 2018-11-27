package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;


public class CredentialFlatListFragment extends CredentialListFragmentBase {

    private CredentialFlatListAdapter listAdapter;


    @Override
    protected int getViewId() {
        return R.layout.navtab_credential_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.credential_flat_list);
        assert recyclerView != null;

        listAdapter = new CredentialFlatListAdapter(this);
        recyclerView.setAdapter(listAdapter);

        credentialListViewModel
                .getRepo()
                .getAllCredentialsSortByName()
                .observe(this, new Observer<List<Credential>>() {
                    @Override
                    public void onChanged(@Nullable final List<Credential> credentials) {
                        listAdapter.setCredentials(credentials);
                    }
                });

        return view;
    }

    @Override
    public void refresh() {
        listAdapter.notifyDataSetChanged();
    }

}

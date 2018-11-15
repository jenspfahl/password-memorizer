package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.ui.BaseFragment;
import de.jepfa.obfusser.ui.credential.input.CredentialInputNameActivity;
import de.jepfa.obfusser.ui.group.assignment.SelectGroupForCredentialActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialListViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;
import de.jepfa.obfusser.viewmodel.template.TemplateListViewModel;


public class CredentialListFragment extends BaseFragment implements View.OnClickListener{

    private CredentialListViewModel credentialListViewModel;
    private GroupListViewModel groupListViewModel;
    private CredentialListAdapter adapter;

    public CredentialListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentialListViewModel = ViewModelProviders
                .of(this)
                .get(CredentialListViewModel.class);

        groupListViewModel = ViewModelProviders
                .of(this.getActivity())
                .get(GroupListViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.navtab_credential_list, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, CredentialInputNameActivity.class);
                startActivity(intent);
            }
        });

        getActivity().setTitle("Credentials");

        RecyclerView recyclerView = view.findViewById(R.id.credential_list);
        assert recyclerView != null;

        adapter = new CredentialListAdapter(
                groupListViewModel, this);
        recyclerView.setAdapter(adapter);


        credentialListViewModel
                .getRepo()
                .getAllCredentialsSortByGroupAndName()
                .observe(this, new Observer<List<Credential>>() {
                    @Override
                    public void onChanged(@Nullable final List<Credential> credentials) {
                        adapter.setCredentials(credentials);
                    }
                });

        return view;
    }

    @Override
    public void onClick(final View v) {
        PopupMenu popup = new PopupMenu(this.getContext(), v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Credential credential = (Credential) v.getTag();
                switch (item.getItemId()) {
                    case R.id.menu_change_credential:
                        Intent intent = new Intent(v.getContext(), CredentialInputNameActivity.class);
                        IntentUtil.setCredentialExtra(intent, credential);
                        startActivity(intent);
                        return true;
                    case R.id.menu_assign_group_credential:
                        intent = new Intent(v.getContext(), SelectGroupForCredentialActivity.class);
                        IntentUtil.setCredentialExtra(intent, credential);
                        startActivity(intent);
                        return true;
                    case R.id.menu_delete_credential:
                        credentialListViewModel.getRepo().delete(credential);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.credential_list_menu);
        popup.show();
    }

    @Override
    public void refresh() {
        adapter.notifyDataSetChanged();
    }
}

package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.BaseFragment;
import de.jepfa.obfusser.ui.credential.input.CredentialInputNameActivity;
import de.jepfa.obfusser.ui.group.assignment.SelectGroupForCredentialActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialListViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;


public abstract class CredentialListFragmentBase extends BaseFragment implements View.OnClickListener{

    private CredentialListViewModel credentialListViewModel;
    private GroupListViewModel groupListViewModel;
    private CredentialExpandableListAdapter expandableAdapter;
    private CredentialListAdapter listAdapter;

    private boolean expandable = true;

    public CredentialListFragmentBase(boolean expandable) {
        this.expandable = expandable;
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

        int viewId = expandable ?
                R.layout.navtab_credential_expandable_list :
                R.layout.navtab_credential_list;
        View view = inflater.inflate(viewId, container, false);

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

        if (expandable) {

            ExpandableListView listView = view.findViewById(R.id.credential_expandable_list);
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
                        }
                    });

            credentialListViewModel
                    .getRepo()
                    .getAllCredentialsSortByGroupAndName()
                    .observe(this, new Observer<List<Credential>>() {
                        @Override
                        public void onChanged(@Nullable final List<Credential> credentials) {
                            expandableAdapter.setCredentials(credentials);
                        }
                    });
        }
        else {
            RecyclerView recyclerView = view.findViewById(R.id.credential_list);
            assert recyclerView != null;

            listAdapter = new CredentialListAdapter(this);
            recyclerView.setAdapter(listAdapter);


            credentialListViewModel
                    .getRepo()
                    .getAllCredentialsSortByGroupAndName()
                    .observe(this, new Observer<List<Credential>>() {
                        @Override
                        public void onChanged(@Nullable final List<Credential> credentials) {
                            listAdapter.setCredentials(credentials);
                        }
                    });
        }

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
        if (expandable) {
            expandableAdapter.notifyDataSetChanged();
        }
        else {
            listAdapter.notifyDataSetChanged();
        }
    }
}

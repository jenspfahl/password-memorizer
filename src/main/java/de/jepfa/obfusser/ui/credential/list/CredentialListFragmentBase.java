package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.ui.credential.input.CredentialInputNameActivity;
import de.jepfa.obfusser.ui.group.assignment.SelectGroupForCredentialActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialListViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;


public abstract class CredentialListFragmentBase extends SecureFragment implements View.OnClickListener{

    protected CredentialListViewModel credentialListViewModel;
    protected GroupListViewModel groupListViewModel;


    protected abstract int getViewId();

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

        View view = inflater.inflate(getViewId(), container, false);

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

}

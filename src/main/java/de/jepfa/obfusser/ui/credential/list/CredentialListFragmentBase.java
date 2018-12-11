package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.ui.credential.input.CredentialInputNameActivity;
import de.jepfa.obfusser.ui.group.assignment.SelectGroupForCredentialActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.EncryptUtil;
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

        setHasOptionsMenu(true);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        MenuItem menuGroupItems = menu.findItem(R.id.menu_group_items);
        if (menuGroupItems != null) {

            boolean expandableList = defaultSharedPreferences
                    .getBoolean(SettingsActivity.PREF_EXPANDABLE_CREDENTIAL_LIST, false);

            if (expandableList) {
                menuGroupItems.setIcon(R.drawable.ic_sort_expanded_white_24dp);
            } else {
                menuGroupItems.setIcon(R.drawable.ic_sort_white_24dp);
            }
        }

        MenuItem menuLockItems = menu.findItem(R.id.menu_lock_items);
        if (menuLockItems != null) {
            boolean passwordCheckEnabled = defaultSharedPreferences
                    .getBoolean(SettingsActivity.PREF_ENABLE_PASSWORD, false);

            if (passwordCheckEnabled) {
                Secret secret = Secret.getOrCreate();
                menuLockItems.setVisible(true);
                if (secret.hasDigest()) {
                    menuLockItems.setIcon(R.drawable.ic_lock_open_white_24dp);
                } else {
                    menuLockItems.setIcon(R.drawable.ic_lock_outline_white_24dp);
                }
            } else {
                menuLockItems.setVisible(false);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        NavigationActivity navigationActivity = (NavigationActivity) getActivity();
        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this.getActivity());

        if (id == R.id.menu_group_items) {

            boolean expandableList = defaultSharedPreferences
                    .getBoolean(SettingsActivity.PREF_EXPANDABLE_CREDENTIAL_LIST, false);

            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putBoolean(SettingsActivity.PREF_EXPANDABLE_CREDENTIAL_LIST, !expandableList);
            editor.commit();

            navigationActivity.refreshContainerFragment();
            return true;
        }

        if (id == R.id.menu_lock_items) {
            boolean passwordCheckEnabled = defaultSharedPreferences
                    .getBoolean(SettingsActivity.PREF_ENABLE_PASSWORD, false);
            if (passwordCheckEnabled) {
                Secret secret = Secret.getOrCreate();
                if (secret.hasDigest()) {
                    secret.invalidate();
                }
                else {
                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
                }
                navigationActivity.refreshContainerFragment();
            }
            return true;
        }

        if (id == R.id.menu_help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("How it works")
                    .setMessage("Blabla")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
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

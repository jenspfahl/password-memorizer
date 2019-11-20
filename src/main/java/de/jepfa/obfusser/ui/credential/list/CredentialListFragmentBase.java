package de.jepfa.obfusser.ui.credential.list;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.ui.common.CommonMenuFragmentBase;
import de.jepfa.obfusser.ui.common.DeletionHelper;
import de.jepfa.obfusser.ui.common.Noogler;
import de.jepfa.obfusser.ui.credential.input.CredentialInputNameActivity;
import de.jepfa.obfusser.ui.group.assignment.SelectGroupForCredentialActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialListViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;


public abstract class CredentialListFragmentBase extends CommonMenuFragmentBase implements View.OnClickListener{

    protected CredentialListViewModel credentialListViewModel;
    protected GroupListViewModel groupListViewModel;


    protected abstract int getViewId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentialListViewModel = ViewModelProviders
                .of(this.getActivity())
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

        getActivity().setTitle(R.string.title_credentials);

        Noogler.INSTANCE.noogleEncryptData(getActivity(), view);

        return view;
    }

    @Override
    protected int getMenuId() {
        return R.menu.toolbar_menu_credential;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        MenuItem menuGroupItems = menu.findItem(R.id.menu_group_items);
        if (menuGroupItems != null) {

            boolean expandableList = defaultSharedPreferences
                    .getBoolean(SettingsActivity.PREF_EXPANDABLE_CREDENTIAL_LIST, false);

            menuGroupItems.setChecked(expandableList);
            if (expandableList) {
                menuGroupItems.setIcon(R.drawable.ic_sort_expanded_white_24dp);
            } else {
                menuGroupItems.setIcon(R.drawable.ic_sort_white_24dp);
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
                        IntentUtil.INSTANCE.setCredentialExtra(intent, credential);
                        startActivity(intent);
                        return true;
                    case R.id.menu_assign_group_credential:
                        intent = new Intent(v.getContext(), SelectGroupForCredentialActivity.class);
                        IntentUtil.INSTANCE.setCredentialExtra(intent, credential);
                        startActivity(intent);
                        return true;
                    case R.id.menu_delete_credential:
                        DeletionHelper.INSTANCE.askAndDelete(credentialListViewModel.getRepo(), credential, getActivity(), null);
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

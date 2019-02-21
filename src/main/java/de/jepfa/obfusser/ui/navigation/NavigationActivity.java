package de.jepfa.obfusser.ui.navigation;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.concurrent.ExecutionException;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.repository.credential.CredentialRepository;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.ui.common.CommonMenuFragmentBase;
import de.jepfa.obfusser.ui.credential.list.CredentialExpandableListFragment;
import de.jepfa.obfusser.ui.credential.list.CredentialFlatListFragment;
import de.jepfa.obfusser.ui.credential.list.CredentialIntroFragment;
import de.jepfa.obfusser.ui.credential.list.CredentialListFragmentBase;
import de.jepfa.obfusser.ui.group.list.GroupListFragment;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.ui.template.list.TemplateListFragment;
import de.jepfa.obfusser.viewmodel.credential.CredentialListViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;
import de.jepfa.obfusser.viewmodel.template.TemplateListViewModel;

public class NavigationActivity extends SecureActivity {

    public static final String SELECTED_NAVTAB = "selected_navtab";

    private int selectedNavId;
    private BottomNavigationView navigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = findViewById(R.id.activity_navigation_toolbar);
        setSupportActionBar(toolbar);

        selectedNavId = getIntent().getIntExtra(SELECTED_NAVTAB, R.id.navigation_credentials);
        if (savedInstanceState != null) {
            selectedNavId = savedInstanceState.getInt(SELECTED_NAVTAB, R.id.navigation_credentials);
        }

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return refreshContainerFragment(item.getItemId());
            }
        });

        refreshContainerFragment();
        navigation.setSelectedItemId(selectedNavId);

    }

    @Override
    protected void onResume() {
        super.onResume();
        selectedNavId = getIntent().getIntExtra(SELECTED_NAVTAB, R.id.navigation_credentials);
        navigation.setSelectedItemId(selectedNavId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_NAVTAB, selectedNavId);
    }

    protected void refresh(boolean before) {
        if (before) {
            for (Fragment currentFragment : getSupportFragmentManager().getFragments()) {
                if (currentFragment instanceof SecureFragment && currentFragment.isVisible()) {
                    ((SecureFragment) currentFragment).refresh();
                }
            }
        }
        else {
            recreate();
        }
    }

    public boolean refreshContainerFragment() {
        return refreshContainerFragment(selectedNavId);
    }

    private boolean refreshContainerFragment(int selectedNavId) {
        this.selectedNavId = selectedNavId;

        switch (selectedNavId) {
            case R.id.navigation_credentials:
            case R.id.navigation_templates:
            case R.id.navigation_groups:
                getIntent().putExtra(SELECTED_NAVTAB, selectedNavId);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.navigation_tab_container, getSelectedFragment(selectedNavId))
                        .commit();
                return true;
            case R.id.navigation_settings:
                Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);

                return true;
        }
        return false;
    }

    private Fragment getSelectedFragment(int selectedNavId) {
        switch (selectedNavId) {
            case R.id.navigation_credentials:
                return getCredentialListFragmentImpl();
            case R.id.navigation_templates:
                return getTemplateListFragmentImpl();
            case R.id.navigation_groups:
                return getGroupListFragmentImpl();
        }

        return getCredentialListFragmentImpl();
    }

    private CommonMenuFragmentBase getCredentialListFragmentImpl() {
        boolean expandableList = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.PREF_EXPANDABLE_CREDENTIAL_LIST, false);
        if (isCredentialsEmpty()) {
            return new CredentialIntroFragment();
        }
        else if (expandableList) {
            return new CredentialExpandableListFragment();
        }
        else {
            return new CredentialFlatListFragment();
        }
    }

    @NonNull
    private CommonMenuFragmentBase getTemplateListFragmentImpl() {
        return new TemplateListFragment();
    }


    @NonNull
    private CommonMenuFragmentBase getGroupListFragmentImpl() {
        return new GroupListFragment();
    }

    private boolean isCredentialsEmpty() {
        CredentialListViewModel credentialListViewModel = ViewModelProviders
                .of(this)
                .get(CredentialListViewModel.class);

        AsyncTask<CredentialListViewModel, Void, Boolean> task = new AsyncTask<CredentialListViewModel, Void, Boolean>() {


            @Override
            protected Boolean doInBackground(CredentialListViewModel... credentialListViewModels) {
                return credentialListViewModels[0].getRepo().getCredentialCountSync() == 0;
            }
        };

        try {
            return task.execute(credentialListViewModel).get();
        } catch (Exception e) {
            Log.e("COUNTCREDENTIALS", "check empty", e);
            return false;
        }
    }

    private boolean isTemplatesEmpty() {
        TemplateListViewModel templateListViewModel = ViewModelProviders
                .of(this)
                .get(TemplateListViewModel.class);

        AsyncTask<TemplateListViewModel, Void, Boolean> task = new AsyncTask<TemplateListViewModel, Void, Boolean>() {


            @Override
            protected Boolean doInBackground(TemplateListViewModel... templateListViewModels) {
                return templateListViewModels[0].getRepo().getTemplateCountSync() == 0;
            }
        };

        try {
            return task.execute(templateListViewModel).get();
        } catch (Exception e) {
            Log.e("COUNTTEMPLATES", "check empty", e);
            return false;
        }
    }

    private boolean isGroupsEmpty() {
        GroupListViewModel groupListViewModel = ViewModelProviders
                .of(this)
                .get(GroupListViewModel.class);

        AsyncTask<GroupListViewModel, Void, Boolean> task = new AsyncTask<GroupListViewModel, Void, Boolean>() {


            @Override
            protected Boolean doInBackground(GroupListViewModel... groupListViewModel) {
                return groupListViewModel[0].getRepo().getGroupCountSync() == 0;
            }
        };

        try {
            return task.execute(groupListViewModel).get();
        } catch (Exception e) {
            Log.e("COUNTGROUPS", "check empty", e);
            return false;
        }
    }


}

package de.jepfa.obfusser.ui.navigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.ui.credential.list.CredentialExpandableListFragment;
import de.jepfa.obfusser.ui.credential.list.CredentialFlatListFragment;
import de.jepfa.obfusser.ui.credential.list.CredentialListFragmentBase;
import de.jepfa.obfusser.ui.group.list.GroupListFragment;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.ui.template.list.TemplateListFragment;

public class NavigationActivity extends SecureActivity {

    public static final String SELECTED_NAVTAB = "selected_navtab";

    private int selectedNavId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = findViewById(R.id.activity_navigation_toolbar);
        setSupportActionBar(toolbar);

        selectedNavId = getIntent().getIntExtra(SELECTED_NAVTAB, R.id.navigation_credentials);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return refreshContainerFragment(item.getItemId());
            }
        });

        navigation.setSelectedItemId(selectedNavId);
        refreshContainerFragment();

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
        switch (selectedNavId) {
            case R.id.navigation_credentials:
                getIntent().putExtra(SELECTED_NAVTAB, R.id.navigation_credentials);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.navigation_tab_container, getCredentialListFragmentImpl())
                        .commit();
                return true;
            case R.id.navigation_templates:
                getIntent().putExtra(SELECTED_NAVTAB, R.id.navigation_templates);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.navigation_tab_container, new TemplateListFragment())
                        .commit();
                return true;
            case R.id.navigation_groups:
                getIntent().putExtra(SELECTED_NAVTAB, R.id.navigation_groups);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.navigation_tab_container, new GroupListFragment())
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
                return new TemplateListFragment();
            case R.id.navigation_groups:
                return new GroupListFragment();
        }

        return getCredentialListFragmentImpl();
    }

    private CredentialListFragmentBase getCredentialListFragmentImpl() {
        boolean expandableList = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.PREF_EXPANDABLE_CREDENTIAL_LIST, false);
        if (expandableList) {
            return new CredentialExpandableListFragment();
        }
        else {
            return new CredentialFlatListFragment();
        }
    }

}

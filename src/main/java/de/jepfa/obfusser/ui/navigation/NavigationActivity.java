package de.jepfa.obfusser.ui.navigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = findViewById(R.id.activity_navigation_toolbar);
        setSupportActionBar(toolbar);

        int selectedNavId = getIntent().getIntExtra(SELECTED_NAVTAB, R.id.navigation_credentials);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(selectedNavId);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();

            Fragment fragment = getSelectedFragment(selectedNavId);
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.navigation_tab_container, fragment)
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_lock_items);
        if (item != null) {
            Secret secret = Secret.getOrCreate();
            item.setVisible(secret.hasDigest());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_group_items) {
            SharedPreferences defaultSharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(this);

            boolean expandableList = defaultSharedPreferences
                    .getBoolean(SettingsActivity.PREF_EXPANDABLE_CREDENTIAL_LIST, false);

            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putBoolean(SettingsActivity.PREF_EXPANDABLE_CREDENTIAL_LIST, !expandableList);
            editor.commit();

            recreate();//TODO maybe to much?
            return true;
        }
        if (id == R.id.menu_lock_items) {
            Secret secret = Secret.getOrCreate();
            if (secret.hasDigest()) {
                secret.invalidate();
                recreate();//TODO maybe to much?
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void refresh(boolean before) {
        for (Fragment currentFragment : getSupportFragmentManager().getFragments()) {
            if (currentFragment instanceof SecureFragment && currentFragment.isVisible()) {
                ((SecureFragment)currentFragment).refresh();
            }
        }
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

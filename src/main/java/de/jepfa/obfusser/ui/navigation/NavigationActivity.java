package de.jepfa.obfusser.ui.navigation;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.service.SecretCheckService;
import de.jepfa.obfusser.ui.BaseActivity;
import de.jepfa.obfusser.ui.BaseFragment;
import de.jepfa.obfusser.ui.credential.list.CredentialListFragment;
import de.jepfa.obfusser.ui.group.list.GroupListFragment;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.ui.template.list.TemplateListFragment;

public class NavigationActivity extends BaseActivity {

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
                            .replace(R.id.navigation_tab_container, new CredentialListFragment())
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
    protected void onStart() {
        super.onStart();
        Intent startServiceIntent = new Intent(this, SecretCheckService.class);
        startService(startServiceIntent);

        scheduleSecretChecker();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

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

    protected void refresh(boolean before) {
        for (Fragment currentFragment : getSupportFragmentManager().getFragments()) {
            if (currentFragment instanceof BaseFragment && currentFragment.isVisible()) {
                ((BaseFragment)currentFragment).refresh();
            }
        }
    }

    private void scheduleSecretChecker() {

        ComponentName serviceComponent = new ComponentName(this, SecretCheckService.class);

        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setPeriodic(2000);
        JobScheduler jobScheduler = getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    private Fragment getSelectedFragment(int selectedNavId) {
        switch (selectedNavId) {
            case R.id.navigation_credentials:
                return new CredentialListFragment();
            case R.id.navigation_templates:
                return new TemplateListFragment();
            case R.id.navigation_groups:
                return new GroupListFragment();
        }

        return new CredentialListFragment();
    }

}

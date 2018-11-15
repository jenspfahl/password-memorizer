package de.jepfa.obfusser.ui.group.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.BaseActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.ui.group.detail.GroupDetailFragment;
import de.jepfa.obfusser.ui.group.input.GroupInputNameActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.group.GroupViewModel;


public class GroupDetailActivity extends BaseActivity {

    private GroupViewModel groupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupViewModel = GroupViewModel.getFromIntent(this, getIntent());
        Group group = groupViewModel.getGroup().getValue();

        Toolbar toolbar = findViewById(R.id.activity_group_detail_toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout appBarLayout = findViewById(R.id.group_detail_toolbar_layout);
        if (appBarLayout != null) {
            StringBuilder sb = new StringBuilder(group.getName());
            appBarLayout.setTitle(sb.toString());
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();

            GroupDetailFragment fragment = new GroupDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.group_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent upIntent = new Intent(this, NavigationActivity.class);
            upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_groups);
            navigateUpTo(upIntent);
            return true;
        }

        Group group = groupViewModel.getGroup().getValue();
        switch (item.getItemId()) {
            case R.id.menu_change_group:
                Intent intent = new Intent(this, GroupInputNameActivity.class);
                IntentUtil.setGroupExtra(intent, group);
                startActivity(intent);
                return true;

            case R.id.menu_delete_group:
                groupViewModel.getRepo().delete(group);

                Intent upIntent = new Intent(this, NavigationActivity.class);
                upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_groups);
                navigateUpTo(upIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(boolean before) {
    }

}

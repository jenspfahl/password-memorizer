package de.jepfa.obfusser.ui.common;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.ui.credential.input.CredentialInputNameActivity;
import de.jepfa.obfusser.ui.group.assignment.SelectGroupForCredentialActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialListViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;


public abstract class CommonMenuFragmentBase extends SecureFragment {


    protected abstract int getMenuId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(getMenuId(), menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

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

        if (id == R.id.menu_legend) {
            LegendShower.showLegend(getActivity(), getSecureActivity().getPatternRepresentation());

            return true;
        }

        if (id == R.id.menu_help) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://password-memorizer.jepfa.de"));
            startActivity(browserIntent);

            return true;
        }

        if (id == R.id.menu_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            Drawable icon = getActivity().getApplicationInfo().loadIcon(getActivity().getPackageManager());
            builder.setTitle(R.string.title_about_the_app)
                    .setMessage(getString(R.string.app_name) + ", Version " + getVersionName(getActivity()) +
                            Constants.NL + " (c) Jens Pfahl 2018,2019")
                    .setIcon(icon)
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getVersionName(Activity activity) {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}

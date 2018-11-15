package de.jepfa.obfusser.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import de.jepfa.obfusser.model.Secret;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseActivity().securityCheck();
    }

    @Override
    public void onResume() {
        super.onResume();

        getBaseActivity().securityCheck();
    }

    public BaseActivity getBaseActivity() {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            return (BaseActivity) activity;
        }
        throw new IllegalStateException("Programming Error, all BaseFragmens should belong to BaseActivity class");
    }

    public abstract void refresh();
}

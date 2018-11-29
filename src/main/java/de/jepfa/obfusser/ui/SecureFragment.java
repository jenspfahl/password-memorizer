package de.jepfa.obfusser.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public abstract class SecureFragment extends Fragment {

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

    public SecureActivity getBaseActivity() {
        FragmentActivity activity = getActivity();
        if (activity instanceof SecureActivity) {
            return (SecureActivity) activity;
        }
        throw new IllegalStateException("Programming Error, all BaseFragmens should belong to SecureActivity class");
    }

    public abstract void refresh();
}

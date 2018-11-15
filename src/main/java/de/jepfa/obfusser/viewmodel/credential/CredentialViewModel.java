package de.jepfa.obfusser.viewmodel.credential;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import java.util.List;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.repository.credential.CredentialRepository;
import de.jepfa.obfusser.util.IntentUtil;

public class CredentialViewModel extends CredentialViewModelBase {

    private MutableLiveData<Credential> credential = new MutableLiveData<>();


    public CredentialViewModel(Application application) {
        super(application);
    }

    public LiveData<Credential> getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential.setValue(credential);
    }

    public static CredentialViewModel getFromIntent(FragmentActivity activity, Intent intent) {
        CredentialViewModel credentialViewModel = get(activity);
        credentialViewModel.setCredential(
                IntentUtil.createCredentialFromIntent(intent));

        return credentialViewModel;
    }

    public static CredentialViewModel get(FragmentActivity activity) {
        return ViewModelProviders
                .of(activity)
                .get(CredentialViewModel.class);
    }
}

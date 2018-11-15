package de.jepfa.obfusser.viewmodel.credential;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

import de.jepfa.obfusser.model.Credential;

public class CredentialListViewModel extends CredentialViewModelBase {

    private LiveData<List<Credential>> credentials;

    public CredentialListViewModel(Application application) {
        super(application);
        credentials = getRepo().getAllCredentialsSortByName();
    }

    public LiveData<List<Credential>> getCredentials() {
        return credentials;
    }
}

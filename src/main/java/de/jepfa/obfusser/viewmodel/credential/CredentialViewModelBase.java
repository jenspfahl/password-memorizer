package de.jepfa.obfusser.viewmodel.credential;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import de.jepfa.obfusser.repository.credential.CredentialRepository;

public abstract class CredentialViewModelBase extends AndroidViewModel {
    private CredentialRepository credentialRepo;


    public CredentialViewModelBase(Application application) {
        super(application);
        credentialRepo = new CredentialRepository(application);
    }

    public CredentialRepository getRepo() {
        return credentialRepo;
    }

}

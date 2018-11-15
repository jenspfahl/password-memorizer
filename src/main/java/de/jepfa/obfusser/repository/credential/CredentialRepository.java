package de.jepfa.obfusser.repository.credential;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import de.jepfa.obfusser.database.ObfusDatabase;
import de.jepfa.obfusser.database.dao.CredentialDao;
import de.jepfa.obfusser.model.Credential;


public class CredentialRepository {

    private CredentialDao credentialDao;

    public CredentialRepository(Application application) {
        ObfusDatabase db = ObfusDatabase.getDatabase(application);
        credentialDao = db.credentialDao();
    }

    public LiveData<List<Credential>> getAllCredentialsSortByName() {
        return credentialDao.getAllCredentialsSortByName();
    }

    public LiveData<List<Credential>> getAllCredentialsSortByGroupAndName() {
        return credentialDao.getAllCredentialsSortByGroupAndName();
    }

    public List<Credential> getAllCredentialsSync() {
        return credentialDao.getAllCredentialsSync();
    }

    public void insert(Credential credential) {
        new InsertAsyncTask(credentialDao).execute(credential);
    }

    public void update(Credential credential) {
        new UpdateAsyncTask(credentialDao).execute(credential);
    }

    public void delete(int credentialId) {
        Credential credential = new Credential();
        credential.setId(credentialId);
        delete(credential);
    }

    public void delete(Credential credential) {
        new DeleteAsyncTask(credentialDao).execute(credential);
    }

    private static class InsertAsyncTask extends AsyncTask<Credential, Void, Void> {

        private CredentialDao asyncTaskDao;

        InsertAsyncTask(CredentialDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Credential... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Credential, Void, Void> {

        private CredentialDao asyncTaskDao;

        UpdateAsyncTask(CredentialDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Credential... params) {
            asyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Credential, Void, Void> {

        private CredentialDao asyncTaskDao;

        DeleteAsyncTask(CredentialDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Credential... params) {
            asyncTaskDao.delete(params[0]);
            return null;
        }
    }

}

package de.jepfa.obfusser.repository.group;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import de.jepfa.obfusser.database.ObfusDatabase;
import de.jepfa.obfusser.database.dao.GroupDao;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.PatternHolder;


public class GroupRepository {

    private GroupDao groupDao;

    public GroupRepository(Application application) {
        ObfusDatabase db = ObfusDatabase.getDatabase(application);
        groupDao = db.groupDao();
    }

    public LiveData<List<Group>> getAllGroupsSortByName() {
        return groupDao.getAllGroupsSortByName();
    }

    public List<Group> getAllGroupsSync() {
        return groupDao.getAllGroupsSync();
    }

    public LiveData<Group> getGroupById(int id) {
        return groupDao.getGroupById(id);
    }

    public LiveData<Group> getGroupFromPattern(PatternHolder pattern) {
        if (pattern.getGroupId() != null) {
            return getGroupById(pattern.getGroupId());
        }
        return new MutableLiveData<>();
    }

    public void insert(Group group) {
        new InsertAsyncTask(groupDao).execute(group);
    }

    public void update(Group group) {
        new UpdateAsyncTask(groupDao).execute(group);
    }

    public void delete(int groupId) {
        Group group = new Group();
        group.setId(groupId);
        delete(group);
    }

    public void delete(Group group) {
        new DeleteAsyncTask(groupDao).execute(group);
    }

    private static class InsertAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao asyncTaskDao;

        InsertAsyncTask(GroupDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao asyncTaskDao;

        UpdateAsyncTask(GroupDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            asyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao asyncTaskDao;

        DeleteAsyncTask(GroupDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            asyncTaskDao.delete(params[0]);
            return null;
        }
    }

}

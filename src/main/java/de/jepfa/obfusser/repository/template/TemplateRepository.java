package de.jepfa.obfusser.repository.template;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import de.jepfa.obfusser.database.ObfusDatabase;
import de.jepfa.obfusser.database.dao.TemplateDao;
import de.jepfa.obfusser.model.Template;


public class TemplateRepository {

    private TemplateDao templateDao;

    public TemplateRepository(Application application) {
        ObfusDatabase db = ObfusDatabase.getDatabase(application);
        templateDao = db.templateDao();
    }

    public LiveData<List<Template>> getAllTemplatesSortByName() {
        return templateDao.getAllTemplatesSortByName();
    }

    public List<Template> getAllTemplatesSync() {
        return templateDao.getAllTemplatesSync();
    }

    public LiveData<List<Template>> getAllTemplatesSortByGroupAndName() {
        return templateDao.getAllTemplatesSortByGroupAndName();
    }

    public int getTemplateCountSync() {
        return templateDao.getTemplateCountSync();
    }

    public LiveData<Template> getTemplateById(int id) {
        return templateDao.getTemplateById(id);
    }

    public void insert(Template template) {
        new InsertAsyncTask(templateDao).execute(template);
    }

    public long insertSync(Template template) {
        return templateDao.insert(template);
    }

    public void update(Template template) {
        new UpdateAsyncTask(templateDao).execute(template);
    }

    public void updateSync(Template template) {
        templateDao.update(template);
    }

    public void delete(int templateId) {
        Template template = new Template();
        template.setId(templateId);
        delete(template);
    }

    public void delete(Template template) {
        new DeleteAsyncTask(templateDao).execute(template);
    }


    private static class InsertAsyncTask extends AsyncTask<Template, Void, Void> {

        private TemplateDao asyncTaskDao;

        InsertAsyncTask(TemplateDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Template... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Template, Void, Void> {

        private TemplateDao asyncTaskDao;

        UpdateAsyncTask(TemplateDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Template... params) {
            asyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Template, Void, Void> {

        private TemplateDao asyncTaskDao;

        DeleteAsyncTask(TemplateDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Template... params) {
            asyncTaskDao.delete(params[0]);
            return null;
        }
    }

}

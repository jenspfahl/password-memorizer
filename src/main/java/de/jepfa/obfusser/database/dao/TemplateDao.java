package de.jepfa.obfusser.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.jepfa.obfusser.model.Template;

@Dao
public interface TemplateDao {

    @Insert
    long insert(Template template);

    @Update
    void update(Template template);

    @Delete
    void delete(Template template);

    @Query("SELECT * FROM Template")
    LiveData<List<Template>> getAllTemplates();

    @Query("SELECT * FROM Template")
    List<Template> getAllTemplatesSync();

    @Query("SELECT count(*) FROM Template")
    int getTemplateCountSync();

    @Query("SELECT * FROM Template WHERE id=:id")
    LiveData<Template> getTemplateById(int id);
}

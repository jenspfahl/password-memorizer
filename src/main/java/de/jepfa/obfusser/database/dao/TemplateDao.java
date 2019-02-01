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
    void insert(Template template);

    @Update
    void update(Template template);

    @Delete
    void delete(Template template);

    @Query("SELECT * FROM Template ORDER BY UPPER(name), id")
    LiveData<List<Template>> getAllTemplatesSortByName();

    @Query("SELECT t.* FROM Template t LEFT JOIN Groups g ON t.group_id=g.id ORDER BY UPPER(g.name), UPPER(t.name), t.id")
    LiveData<List<Template>> getAllTemplatesSortByGroupAndName();

    @Query("SELECT * FROM Template")
    List<Template> getAllTemplatesSync();

    @Query("SELECT * FROM Template WHERE id=:id")
    LiveData<Template> getTemplateById(int id);
}

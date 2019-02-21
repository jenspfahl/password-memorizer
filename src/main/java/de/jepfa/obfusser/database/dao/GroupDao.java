package de.jepfa.obfusser.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.jepfa.obfusser.model.Group;

@Dao
public interface GroupDao {

    @Insert
    long insert(Group group);

    @Update
    void update(Group group);

    @Delete
    void delete(Group group);

    @Query("SELECT * FROM Groups ORDER BY UPPER(name), id")
    LiveData<List<Group>> getAllGroupsSortByName();

    @Query("SELECT * FROM Groups")
    List<Group> getAllGroupsSync();

    @Query("SELECT * FROM Groups WHERE id=:id")
    LiveData<Group> getGroupById(int id);

    @Query("SELECT count(*) FROM Groups")
    int getGroupCountSync();
}

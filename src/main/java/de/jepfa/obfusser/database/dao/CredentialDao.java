package de.jepfa.obfusser.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.jepfa.obfusser.model.Credential;

@Dao
public interface CredentialDao {

    @Insert
    void insert(Credential credential);

    @Update
    void update(Credential credential);

    @Delete
    void delete(Credential credential);

    @Query("SELECT * FROM Credential ORDER BY UPPER(name), id")
    LiveData<List<Credential>> getAllCredentialsSortByName();

    @Query("SELECT c.* FROM Credential c LEFT JOIN Groups g ON c.group_id=g.id ORDER BY UPPER(g.name), UPPER(c.name), c.id")
    LiveData<List<Credential>> getAllCredentialsSortByGroupAndName();

    @Query("SELECT * FROM Credential")
    List<Credential> getAllCredentialsSync();

}

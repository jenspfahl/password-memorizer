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
    long insert(Credential credential);

    @Update
    void update(Credential credential);

    @Delete
    void delete(Credential credential);

    @Query("SELECT * FROM Credential")
    LiveData<List<Credential>> getAllCredentials();

    @Query("SELECT * FROM Credential")
    List<Credential> getAllCredentialsSync();

    @Query("SELECT count(*) FROM Credential")
    int getCredentialCountSync();

}

package de.jepfa.obfusser.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import de.jepfa.obfusser.database.dao.CredentialDao;
import de.jepfa.obfusser.database.dao.GroupDao;
import de.jepfa.obfusser.database.dao.TemplateDao;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.Template;

@Database(entities = {Credential.class, Template.class, Group.class}, version = 4, exportSchema = false)
@TypeConverters(IntegerStringMapConverter.class)
public abstract class ObfusDatabase extends RoomDatabase {

    public abstract CredentialDao credentialDao();

    public abstract TemplateDao templateDao();

    public abstract GroupDao groupDao();

    private static volatile ObfusDatabase INSTANCE;

    public static ObfusDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ObfusDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ObfusDatabase.class, "obfus_database")
                           // .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

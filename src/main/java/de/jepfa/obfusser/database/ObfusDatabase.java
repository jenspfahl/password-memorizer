package de.jepfa.obfusser.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import de.jepfa.obfusser.database.converter.CryptStringConverter;
import de.jepfa.obfusser.database.converter.IntegerStringMapConverter;
import de.jepfa.obfusser.database.dao.CredentialDao;
import de.jepfa.obfusser.database.dao.GroupDao;
import de.jepfa.obfusser.database.dao.TemplateDao;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.Template;

@Database(entities = {Credential.class, Template.class, Group.class}, version = 5, exportSchema = false)
@TypeConverters({IntegerStringMapConverter.class, CryptStringConverter.class})
public abstract class ObfusDatabase extends RoomDatabase {

    public abstract CredentialDao credentialDao();

    public abstract TemplateDao templateDao();

    public abstract GroupDao groupDao();

    private static volatile ObfusDatabase INSTANCE;

    public static ObfusDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ObfusDatabase.class) {
                if (INSTANCE == null) {
                    Migration migration = new Migration(4, 5) {
                        @Override
                        public void migrate(@NonNull SupportSQLiteDatabase database) {
                            database.execSQL("ALTER TABLE Credential ADD COLUMN uuid TEXT");
                            database.execSQL("ALTER TABLE Template ADD COLUMN uuid TEXT");
                        }
                    };
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ObfusDatabase.class, "obfus_database")
                            .addMigrations(migration)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

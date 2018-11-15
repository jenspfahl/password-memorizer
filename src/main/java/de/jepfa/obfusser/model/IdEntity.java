package de.jepfa.obfusser.model;

import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Objects;

/**
 * An entity class with an unique Id.
 *
 * @author Jens Pfahl
 */
public abstract class IdEntity {

    public static final String ATTRIB_ID = "id";

    @PrimaryKey(autoGenerate = true)
    @NonNull
    protected int id;

    @NonNull
    public int getId() {
        return id;
    }

    public void setId(@NonNull int id) {
        this.id = id;
    }

    public boolean isPersisted() {
        return getId() != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdEntity)) return false;
        IdEntity idEntity = (IdEntity) o;
        return id == idEntity.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}

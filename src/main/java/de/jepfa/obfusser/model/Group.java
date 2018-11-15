package de.jepfa.obfusser.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * A group to classify certain objects.
 *
 * @author Jens Pfahl
 */
@Entity(tableName = "Groups")
public class Group extends IdEntity {

    public static final String ATTRIB_NAME = "name";
    public static final String ATTRIB_INFO = "info";
    public static final String ATTRIB_COLOR = "color";

    @NonNull
    private String name;

    @Nullable
    private String info;

    @Nullable
    private int color;


    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getInfo() {
        return info;
    }

    public void setInfo(@Nullable String info) {
        this.info = info;
    }

    @Nullable
    public int getColor() {
        return color;
    }

    public void setColor(@Nullable int color) {
        this.color = color;
    }

}

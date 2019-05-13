package de.jepfa.obfusser.model;

import android.arch.persistence.room.Entity;
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
    private CryptString name;

    @Nullable
    private CryptString info;

    @Nullable
    private int color;


    @NonNull
    public CryptString getName() {
        return name;
    }

    public void setName(@NonNull CryptString name) {
        this.name = name;
    }

    @Nullable
    public CryptString getInfo() {
        return info;
    }

    public void setInfo(@Nullable CryptString info) {
        this.info = info;
    }

    @Nullable
    public int getColor() {
        return color;
    }

    public void setColor(@Nullable int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Group{");
        sb.append("id=").append(getId());
        sb.append(", name='").append(CryptString.toDebugString(name)).append('\'');
        sb.append(", info='").append(CryptString.toDebugString(info)).append('\'');
        sb.append(", color=").append(color);
        sb.append('}');
        return sb.toString();
    }
}

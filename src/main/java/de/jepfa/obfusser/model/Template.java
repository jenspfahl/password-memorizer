package de.jepfa.obfusser.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;

/**
 * A template is used to create {@link Credential}s from the almost same password
 * or other.
 *
 * @author Jens Pfahl
 */
@Entity(indices =
            {@Index(value = {"group_id"})},
        foreignKeys = {
        @ForeignKey(entity = Group.class,
                parentColumns = "id",
                childColumns = "group_id",
                onDelete = ForeignKey.SET_NULL)})
public class Template extends SecurePatternHolder {

}

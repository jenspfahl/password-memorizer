package de.jepfa.obfusser.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.jepfa.obfusser.Constants;

/**
 * Base class for {@link Credential} and {@link Template}.
 *
 * @author Jens Pfahl
 */
public abstract class PatternHolder extends IdEntity {

    public static final String ATTRIB_NAME = "name";
    public static final String ATTRIB_INFO = "info";
    public static final String ATTRIB_PATTERN_INTERNAL = "pattern_internal";
    public static final String ATTRIB_HINTS = "hints";
    public static final String ATTRIB_GROUP_ID = "group_id";


    @NonNull
    private String name;

    @Nullable
    private String info;

    @NonNull
    private String patternInternal;

    @NonNull
    private Map<Integer, String> hints;

    @Nullable
    @ColumnInfo(name = "group_id")
    private Integer groupId;

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

    public void setInfo(@NonNull String info) {
        this.info = info;
    }

    @Nullable
    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(@Nullable Integer groupId) {
        this.groupId = groupId;
    }

    @NonNull
    public String getPatternInternal() {
        return patternInternal;
    }

    public void setPatternInternal(@NonNull String patternInternal) {
        this.patternInternal = patternInternal;
    }

    /**
     * Not encrypted!!! Use {@link SecurePatternHolder#getHints(byte[])} instead.
     */
    @NonNull
    public Map<Integer, String> getHints() {
        if (hints == null) {
            hints = new TreeMap<>();
        }
        else if (!(hints instanceof TreeMap)) {
            hints = new TreeMap<>(hints);
        }

        return hints;
    }

    public int getHintsCount() {
        return getHints().size();
    }

    /**
     * Use only for copy data.
     */
    public void setHints(@NonNull Map<Integer, String> hints) {
        this.hints = hints;
    }

    @Ignore
    public int getPatternLength() {
        if (patternInternal == null) {
            return 0;
        }
        return patternInternal.length();
    }


    public void copyFrom(PatternHolder other) {
        setPatternInternal(other.getPatternInternal());
        setHints(new TreeMap<>(other.getHints()));
        //setGroupId(other.getGroupId()); TODO activate if group is part of the cretion/change process
    }

    public String toString() {
        return "id=" + id +
                ", name='" + name + '\'' +
                ", info='" + info + '\'' +
                ", patternInternal='" + patternInternal + '\'' +
                ", hints=" + hints +
                ", groupId=" + groupId;
    }

}

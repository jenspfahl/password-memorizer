package de.jepfa.obfusser.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;
import java.util.TreeMap;

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
    private CryptString name;

    @Nullable
    private CryptString info;

    @NonNull
    private CryptString patternInternal;

    @NonNull
    private Map<Integer, String> hints;

    @Nullable
    @ColumnInfo(name = "group_id")
    private Integer groupId;

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

    public void setInfo(@NonNull CryptString info) {
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
    public CryptString getPatternInternal() {
        return patternInternal;
    }

    public void setPatternInternal(@NonNull CryptString patternInternal) {
        this.patternInternal = patternInternal;
    }

    /**
     * Not encrypted!!! Use {@link SecurePatternHolder#getHints(byte[], boolean)} instead.
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
        return getPatternInternal().length();
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

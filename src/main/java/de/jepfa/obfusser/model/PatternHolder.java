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
        cutOverlapingHints(patternInternal != null ? patternInternal.length() : 0);
        this.patternInternal = patternInternal;
    }

    /**
     * Not sorted nor encrypted!!! Use {@link SecurePatternHolder} instead.
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

    /**
     * Use only for copy data.
     */
    public void setHints(@NonNull Map<Integer, String> hints) {
        this.hints = hints;
    }

    @Ignore
    public Pair<Integer, String> getHintDataByPosition(int position) {
        int count = 0;
        Map<Integer, String> hints = getHints();
        for (Map.Entry<Integer, String> entry : hints.entrySet()) {
            if (position == count) {
                return new Pair<>(entry.getKey(), entry.getValue());
            }
            count++;
        }
        return null;
    }

    @Ignore
    public NumberedPlaceholder getNumberedPlaceholder(int index) {
        int placeholder = 1;
        for (Map.Entry<Integer, String> entry : getHints().entrySet()) {
            if (index == entry.getKey()) {
                return NumberedPlaceholder.fromPlaceholderNumber(placeholder);
            }
            placeholder++;
        }
        return null;
    }


    @Ignore
    public boolean hasHint(int index) {
        return getHints().containsKey(index);
    }

    @Ignore
    public boolean isPotentialHint(int index) {
        return getHints().containsKey(index) && getHints().get(index).equals(Constants.EMPTY);
    }

    @Ignore
    public boolean isFilledHint(int index) {
        return getHints().containsKey(index) && !getHints().get(index).isEmpty();
    }


    @Ignore
    public void addPotentialHint(int index) {
        getHints().put(index, Constants.EMPTY);
    }


    @Ignore
    public String removePotentialHint(int index) {
        return getHints().remove(index);
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
        setHints(mergeHints(other.getHints()));
        //setGroupId(other.getGroupId()); TODO activate if group is part of the cretion/change process
    }

    public void mergeHintsIntoPattern() {

        for (Map.Entry<Integer, String> entry : getHints().entrySet()) {
            Integer index = entry.getKey();
            replacePatternWithPlaceholder(index, index + 1);
        }
    }

    public String toString() {
        return "Pattern{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", info='" + info + '\'' +
                ", patternInternal='" + patternInternal + '\'' +
                ", hints=" + hints +
                ", groupId=" + groupId +
                '}';
    }

    private Map<Integer,String> mergeHints(Map<Integer, String> templateHints) {
        Map<Integer, String> newHints = new HashMap<>(templateHints);

        Map<Integer, String> credentialHints = getHints();
        if (!credentialHints.isEmpty() && !templateHints.isEmpty()) {
            int i = 0;
            for (Map.Entry<Integer, String> entry : templateHints.entrySet()) {
                Pair<Integer, String> hintData = getHintDataByPosition(i);
                if (hintData != null) {
                    newHints.put(entry.getKey(), hintData.second);
                }
                i++;
            }
        }
        return newHints;
    }



    private void cutOverlapingHints(int patternSize) {
        if (patternSize == 0) {
            getHints().clear();
        }
        else {
            Set<Integer> deleteCandidates = new HashSet<>();
            for (Integer hintIndex : getHints().keySet()) {
                if (hintIndex >= patternSize) {
                    deleteCandidates.add(hintIndex);
                }
            }
            for (Integer deleteCandidate : deleteCandidates) {
                getHints().remove(deleteCandidate);
            }
        }
    }

    private void replacePatternWithPlaceholder(int start, int end) {
        String patternInternal = getPatternInternal();
        if (patternInternal != null) {
            ObfusString pattern = ObfusString.fromExchangeFormat(patternInternal);

            pattern.replaceWithPlaceholder(start, end);
            setPatternInternal(pattern.toExchangeFormat());
        }
    }

}

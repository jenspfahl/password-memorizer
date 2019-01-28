package de.jepfa.obfusser.model;

import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.util.encrypt.EncryptUtil;

/**
 * Base class for {@link Credential} and {@link Template} but adding
 * functionality for on-the-fly en-/decryption.
 *
 * @author Jens Pfahl
 */
public abstract class SecurePatternHolder extends PatternHolder {

    public static final String ATTRIB_UUID = "uuid";


    @Nullable
    private String uuid;

    @Nullable
    public synchronized String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    public synchronized void setUuid(@Nullable String uuid) {
        this.uuid = uuid;
    }

    public String getPatternAsExchangeFormatHinted(byte[] key, boolean encWithUuid) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        ObfusString pattern = getPattern(key, encWithUuid);
        if (pattern != null && pattern.getObfusChars() != null) {
            for (ObfusChar obfusChar : pattern.getObfusChars()) {
                String s = obfusChar.toExchangeFormat();
                String hint = getHint(index, key, encWithUuid);
                if (hint != null) {
                    if (hint.equals(Constants.EMPTY)) {
                        //TODO not set hints as special char for representation
                        //TODO nothing, use char from pattern // s = ObfusChar.SPECIAL_CHAR.toExchangeFormat();
                    }
                    else {
                        s = ObfusString.obfuscate(hint).toExchangeFormat();
                    }
                }
                sb.append(s);
                index++;
            }
        }
        return sb.toString();
    }

    @Ignore
    public String getPatternRepresentationWithNumberedPlaceholder(byte[] key, Representation representation, boolean encWithUuid) {
        if (key == Secret.INVALID_DIGEST) {
            return getHiddenPatternRepresentation(representation);
        }

        StringBuilder sb = new StringBuilder(getPattern(key, encWithUuid).toRepresentation(representation));
        int placeholder = 1;
        for (Map.Entry<Integer, String> entry : getHints(key, encWithUuid).entrySet()) {
            int index = entry.getKey();
            sb.replace(index, index + 1, NumberedPlaceholder.fromPlaceholderNumber(placeholder).toRepresentation());
            placeholder++;
        }
        return sb.toString();
    }

    @Ignore
    public String getPatternRepresentationHinted(byte[] key, Representation representation, boolean encWithUuid) {
        if (key == Secret.INVALID_DIGEST) {
            return getHiddenPatternRepresentation(representation);
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        if (getPattern(key, encWithUuid) != null && getPattern(key, encWithUuid).getObfusChars() != null) {
            for (ObfusChar obfusChar : getPattern(key, encWithUuid).getObfusChars()) {
                String s = obfusChar.toRepresentation(representation);
                String hint = getHint(index, key, encWithUuid);
                if (hint != null) {
                    if (hint.equals(Constants.EMPTY)) {
                        //TODO not set hints as special char for representation
                        //TODO nothing, use char from pattern // s = ObfusChar.SPECIAL_CHAR.toRepresentation(representation);
                    }
                    else {
                        s = ObfusString.obfuscate(hint).toRepresentation(representation);
                    }
                }
                sb.append(s);
                index++;
            }
        }
        return sb.toString();
    }

    @Ignore
    public String getPatternRepresentationRevealed(byte[] key, Representation representation, boolean encWithUuid) {
        if (key == Secret.INVALID_DIGEST) {
            return getHiddenPatternRepresentation(representation);
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (ObfusChar obfusChar : getPattern(key, encWithUuid).getObfusChars()) {
            String s = obfusChar.toRepresentation(representation);
            String hint = getHint(index, key, encWithUuid);
            if (hint != null) {
                s = hint;
            }
            sb.append(s);
            index++;
        }
        return sb.toString();
    }


    @Ignore
    public void setPatternFromUser(String userInput, byte[] key, boolean encWithUuid) {
        if (userInput != null) {
            setPattern(ObfusString.obfuscate(userInput), key, encWithUuid);
        }
    }

    @Ignore
    public void setPatternFromExchangeFormat(String pattern, byte[] key, boolean encWithUuid) {
        if (pattern != null) {
            setPattern(ObfusString.obfuscate(pattern), key, encWithUuid);
        }
    }

    /**
     *
     * @param index the real (UI) index
     * @param key
     * @return
     */
    @Ignore
    public String getHint(int index, byte[] key, boolean encWithUuid) {
        if (key == Secret.INVALID_DIGEST) {
            return null;
        }
        byte[] uuidKey = getUUIDKey(key, encWithUuid);
        int encryptedIndex = EncryptUtil.encryptIndex(index, getPatternLength(), uuidKey);
        String hint = getHints().get(encryptedIndex);
        return EncryptUtil.decryptHint(hint, encryptedIndex, uuidKey);
    }

    @Ignore
    public Map<Integer, String> getHints(byte[] key, boolean encWithUuid) {
        if (key == Secret.INVALID_DIGEST) {
            return Collections.emptyMap();
        }
        Map<Integer, String> hints = new TreeMap<>();
        byte[] uuidKey = getUUIDKey(key, encWithUuid);

        for (Map.Entry<Integer, String> entry : getHints().entrySet()) {
            Integer decryptedIndex = EncryptUtil.decryptIndex(entry.getKey(), getPatternLength(), uuidKey);
            String decryptedHint = EncryptUtil.decryptHint(entry.getValue(), entry.getKey(), uuidKey);

            hints.put(decryptedIndex, decryptedHint);
        }

        return hints;
    }

    /**
     *
     * @param index the real (UI) index
     * @param key
     * @return
     */
    @Ignore
    public NumberedPlaceholder getNumberedPlaceholder(int index, byte[] key, boolean encWithUuid) {
        int placeholder = 1;
        for (Map.Entry<Integer, String> entry : getHints(key, encWithUuid).entrySet()) {
            if (index == entry.getKey()) {
                return NumberedPlaceholder.fromPlaceholderNumber(placeholder);
            }
            placeholder++;
        }
        return null;
    }


    /**
     *
     * @param index the real (UI) index
     * @param key
     * @return
     */
    @Ignore
    public boolean hasHint(int index, byte[] key, boolean encWithUuid) {
        Integer encryptedIndex = EncryptUtil.encryptIndex(index, getPatternLength(), getUUIDKey(key, encWithUuid));
        return getHints().containsKey(encryptedIndex);
    }

    /**
     *
     * @param index the real (UI) index
     * @param key
     * @return
     */
    @Ignore
    public boolean isFilledHint(int index, byte[] key, boolean encWithUuid) {
        Integer encryptedIndex = EncryptUtil.encryptIndex(index, getPatternLength(), getUUIDKey(key, encWithUuid));
        return getHints().containsKey(encryptedIndex) && !getHints().get(encryptedIndex).isEmpty();
    }


    /**
     *
     * @param index the real (UI) index
     * @param key
     */
    @Ignore
    public void addHint(int index, byte[] key, boolean encWithUuid) {
        Integer encryptedIndex = EncryptUtil.encryptIndex(index, getPatternLength(), getUUIDKey(key, encWithUuid));
        getHints().put(encryptedIndex, Constants.EMPTY);
    }

    /**
     *
     * @param index the real (UI) index
     * @param value
     * @param key
     */
    @Ignore
    public void setHint(int index, String value, byte[] key, boolean encWithUuid) {
        byte[] uuidKey = getUUIDKey(key, encWithUuid);
        Integer encryptedIndex = EncryptUtil.encryptIndex(index, getPatternLength(), uuidKey);
        getHints().put(encryptedIndex, EncryptUtil.encryptHint(value, encryptedIndex, uuidKey));
    }

    /**
     *
     * @param index the real (UI) index
     * @param key
     * @return
     */
    @Ignore
    public String removeHint(int index, byte[] key, boolean encWithUuid) {
        Integer encryptedIndex = EncryptUtil.encryptIndex(index, getPatternLength(), getUUIDKey(key, encWithUuid));
        return getHints().remove(encryptedIndex);
    }

    @Ignore
    public Pair<Integer, String> getHintDataByPosition(int position, byte[] key, boolean encWithUuid) {
        if (key == Secret.INVALID_DIGEST) {
            return null;
        }
        int count = 0;
        Map<Integer, String> hints = getHints(key, encWithUuid);
        for (Map.Entry<Integer, String> entry : hints.entrySet()) {
            if (position == count) {
                return new Pair<>(entry.getKey(), entry.getValue());
            }
            count++;
        }
        return null;
    }

    @Ignore
    public byte[] getUUIDKey(byte[] secret, boolean doit) {
        if (secret == null) {
            return null;
        }
        if (!doit) {
            return secret;
        }
        return EncryptUtil.genUUIDKey(secret, getUuid());
    }


    @Ignore
    public void encrypt(byte[] key, boolean encWithUuid) {
        setPattern(getPattern(null, encWithUuid), key, encWithUuid); // load as is and save encrypted

        if (key != null) {
            synchronized (this) {
                Map<Integer, String> originHints = getHints();
                Map<Integer, String> newHints = new TreeMap<>();
                byte[] uuidKey = getUUIDKey(key, encWithUuid);
                for (Map.Entry<Integer, String> entry : originHints.entrySet()) {
                    int encryptedIndex = EncryptUtil.encryptIndex(entry.getKey(), getPatternLength(), uuidKey);
                    // encrypt hint data with encrypted index
                    String encryptedHint = EncryptUtil.encryptHint(entry.getValue(), encryptedIndex, uuidKey);
                    newHints.put(encryptedIndex, encryptedHint);
                }
                setHints(newHints);
            }
        }
    }

    @Ignore
    public void decrypt(byte[] key, boolean encWithUuid) {
        setPattern(getPattern(key, encWithUuid), null, encWithUuid); // load decrypted and save as is

        if (key != null) {
            synchronized (this) {
                Map<Integer, String> originHints = getHints();
                Map<Integer, String> newHints = new TreeMap<>();
                byte[] uuidKey = getUUIDKey(key, encWithUuid);

                for (Map.Entry<Integer, String> entry : originHints.entrySet()) {
                    // decrypt hint data with encrypted index
                    String decryptedHint = EncryptUtil.decryptHint(entry.getValue(), entry.getKey(), uuidKey);
                    int decryptedIndex = EncryptUtil.decryptIndex(entry.getKey(), getPatternLength(), uuidKey);
                    newHints.put(decryptedIndex, decryptedHint);
                }
                setHints(newHints);
            }
        }

    }

    private ObfusString getPattern(byte[] key, boolean encWithUuid) {
        ObfusString pattern = ObfusString.fromExchangeFormat(getPatternInternal());

        if (pattern != null && key != null) {
            pattern.decrypt(getUUIDKey(key, encWithUuid));
        }
        return pattern;
    }


    private void setPattern(@NonNull ObfusString pattern, byte[] key, boolean encWithUuid) {

        recryptAllHints(getPatternLength(), pattern.length(), key, encWithUuid);

        ObfusString tbs = new ObfusString(pattern);
        if (key != null) {
            tbs.encrypt(getUUIDKey(key, encWithUuid));
        }
        setPatternInternal(tbs.toExchangeFormat());
    }

    public String getHiddenPatternRepresentation(Representation representation) {
        return new ObfusString(
                Arrays.asList(new ObfusChar[]{
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR,
                        ObfusChar.ANY_CHAR
                })).toRepresentation(representation);
    }



    private void recryptAllHints(int oldPatternLength, int newPatternLength, byte[] key, boolean encWithUuid) {
        if (newPatternLength == 0) {
            getHints().clear();
        }
        else {
            if (getHintsCount() != 0) {
                Map<Integer, String> newHints = new HashMap<>();
                byte[] uuidKey = getUUIDKey(key, encWithUuid);
                for (Map.Entry<Integer, String> entry : getHints().entrySet()) {
                    int decryptedIndex = EncryptUtil.decryptIndex(entry.getKey(), oldPatternLength, uuidKey);

                    if (decryptedIndex < newPatternLength) {
                        int encryptedIndex = EncryptUtil.encryptIndex(decryptedIndex, newPatternLength, uuidKey);
                        newHints.put(encryptedIndex, entry.getValue());
                    }

                }
                setHints(newHints);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString()
                + ", uuid='" + uuid;
    }
}

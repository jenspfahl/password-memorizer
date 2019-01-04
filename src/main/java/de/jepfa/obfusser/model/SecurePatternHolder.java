package de.jepfa.obfusser.model;

import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import de.jepfa.obfusser.util.EncryptUtil;

/**
 * Base class for {@link Credential} and {@link Template} but adding
 * functionality for on-the-fly en-/decryption.
 *
 * @author Jens Pfahl
 */
public abstract class SecurePatternHolder extends PatternHolder {


    public String getPatternAsExchangeFormatHinted(byte[] key) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        if (getPattern(key) != null && getPattern(key).getObfusChars() != null) {
            for (ObfusChar obfusChar : getPattern(key).getObfusChars()) {
                String s = obfusChar.toExchangeFormat();
                if (obfusChar.isPlaceholder()) {
                    String hint = getHint(index, key);
                    if (hint != null) {
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
    public String getPatternRepresentationWithNumberedPlaceholder(byte[] key, Representation representation) {
        if (key == Secret.INVALID_DIGEST) {
            return getHiddenPatternRepresentation(representation);
        }

        StringBuilder sb = new StringBuilder(getPattern(key).toRepresentation(representation));
        int placeholder = 1;
        for (Map.Entry<Integer, String> entry : getHints(key).entrySet()) {
            int index = entry.getKey();
            sb.replace(index, index + 1, NumberedPlaceholder.fromPlaceholderNumber(placeholder).toRepresentation());
            placeholder++;
        }
        return sb.toString();
    }

    @Ignore
    public String getPatternRepresentationHinted(byte[] key, Representation representation) {
        if (key == Secret.INVALID_DIGEST) {
            return getHiddenPatternRepresentation(representation);
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        if (getPattern(key) != null && getPattern(key).getObfusChars() != null) {
            for (ObfusChar obfusChar : getPattern(key).getObfusChars()) {
                String s = obfusChar.toRepresentation(representation);
                if (obfusChar.isPlaceholder()) {
                    String hint = getHint(index, key);
                    if (hint != null) {
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
    public String getPatternRepresentationRevealed(byte[] key, Representation representation) {
        if (key == Secret.INVALID_DIGEST) {
            return getHiddenPatternRepresentation(representation);
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (ObfusChar obfusChar : getPattern(key).getObfusChars()) {
            String s = obfusChar.toRepresentation(representation);
            if (obfusChar.isPlaceholder()) {
                String hint = getHint(index, key);
                if (hint != null) {
                    s = hint;
                }
            }
            sb.append(s);
            index++;
        }
        return sb.toString();
    }


    @Ignore
    public void setPatternFromUser(String userInput, byte[] key) {
        if (userInput != null) {
            setPattern(ObfusString.obfuscate(userInput), key);
        }
    }

    @Ignore
    public String getHint(int index, byte[] key) {
        if (key == Secret.INVALID_DIGEST) {
            return null;
        }
        String hint = getHints().get(index);
        return EncryptUtil.decryptPlainString(hint, key);
    }

    @Ignore
    public Map<Integer, String> getHints(byte[] key) {
        if (key == Secret.INVALID_DIGEST) {
            return Collections.emptyMap();
        }
        Map<Integer, String> hints = new TreeMap<>(getHints());

        for (Map.Entry<Integer, String> entry : hints.entrySet()) {
            String decryptedHint = EncryptUtil.decryptPlainString(entry.getValue(), key);
            hints.put(entry.getKey(), decryptedHint);
        }

        return hints;
    }

    @Ignore
    public Pair<Integer, String> getHintDataByPosition(int position, byte[] key) {
        if (key == Secret.INVALID_DIGEST) {
            return null;
        }
        Pair<Integer, String> pair = super.getHintDataByPosition(position);
        if (pair != null) {
            return new Pair<>(pair.first, EncryptUtil.decryptPlainString(pair.second, key));
        }

        return null;
    }

    @Ignore
    public void setPotentialHint(int index, String value, byte[] key) {
        getHints().put(index, EncryptUtil.encryptPlainString(value, key));
    }

    @Ignore
    public void encrypt(byte[] key) {
        setPattern(getPattern(null), key); // load as is and save encrypted

        if (key != null) {
            for (Map.Entry<Integer, String> entry : getHints().entrySet()) {
                String encryptedHint = EncryptUtil.encryptPlainString(entry.getValue(), key);
                getHints().put(entry.getKey(), encryptedHint);
            }

        }
    }

    @Ignore
    public void decrypt(byte[] key) {
        setPattern(getPattern(key), null); // load decrypted and save as is

        if (key != null) {
            for (Map.Entry<Integer, String> entry : getHints().entrySet()) {
                String decryptedHint = EncryptUtil.decryptPlainString(entry.getValue(), key);
                getHints().put(entry.getKey(), decryptedHint);
            }

        }

    }

    private ObfusString getPattern(byte[] key) {
        ObfusString pattern = ObfusString.fromExchangeFormat(getPatternInternal());

        if (pattern != null && key != null) {
            pattern.decrypt(key);
        }
        return pattern;
    }


    private void setPattern(@NonNull ObfusString pattern, byte[] key) {

        if (pattern != null) {
            ObfusString tbs = new ObfusString(pattern);
            if (key != null) {
                tbs.encrypt(key);
            }
            setPatternInternal(tbs.toExchangeFormat());
        }
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
}

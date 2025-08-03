package de.jepfa.obfusser.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.jepfa.obfusser.util.encrypt.EncryptUtil;

/**
 * This class representens an obfuscated string.
 *
 * @see ObfusChar
 *
 * @author Jens Pfahl
 */
public class ObfusString {

    public static final int FIXED_OBFUS_MIN_LENGTH = 8;
    public static final int FIXED_OBFUS_MAX_LENGTH = 16;

    /**
     * Obfuscates the given string.
     *
     * @param string
     * @return
     */
    public static ObfusString obfuscate(String string) {
        List<ObfusChar> obfusChars = new ArrayList<>(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            obfusChars.add(ObfusChar.obfuscate(c));
        }

        return new ObfusString(obfusChars, null);
    }

    /**
     * Creates an {@link ObfusString} from the given exchange format string.
     *
     * @param string
     * @return
     */
    public static ObfusString fromExchangeFormat(String string, @Nullable Integer obfusLength) {
        if (string == null) {
            return null;
        }
        List<ObfusChar> obfusChars = new ArrayList<>(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            obfusChars.add(ObfusChar.fromExchangeFormat(c));
        }

        return new ObfusString(obfusChars, obfusLength);
    }

    /**
     * Creates an exchange format string from the given {@link ObfusString}.
     *
     * @param obfusString
     * @return
     */
    public static String toExchangeFormat(ObfusString obfusString) {
        if (obfusString == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (ObfusChar obfusChar : obfusString.getObfusChars()) {
            sb.append(obfusChar.toExchangeFormat());
        }
        return sb.toString();
    }

    /**
     * Creates an {@link ObfusString} from the given representation string.
     *
     * @param string
     * @param  representation the current used {@link Representation}
     * @return
     */
    public static ObfusString fromRepresentation(String string, Representation representation
            , @Nullable Integer obfusLength) {
        if (string == null) {
            return null;
        }

        List<ObfusChar> obfusChars = new ArrayList<>(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            obfusChars.add(ObfusChar.fromRepresentation(c, representation));
        }

        return new ObfusString(obfusChars, obfusLength);
    }

    /**
     * Creates a fancy representation string from the given {@link ObfusString}.
     *
     * @param obfusString
     * @return
     */
    public static String toRepresentation(ObfusString obfusString, Representation representation) {
        if (obfusString == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (ObfusChar obfusChar : obfusString.getObfusChars()) {
            sb.append(obfusChar.toRepresentation(representation));
        }
        return sb.toString();
    }


    private List<ObfusChar> obfusChars;

    private Integer obfusLength;

    /**
     * Copy constructor.
     *
     * @param other
     */
    public ObfusString(@NonNull ObfusString other) {
        this(other.getObfusChars(), other.obfusLength);
    }

    /**
     * Constructor.
     *
     * @param obfusChars
     * @see ObfusChar
     */
    public ObfusString(@NonNull List<ObfusChar> obfusChars, @Nullable Integer obfusLength) {
        this.obfusChars = new ArrayList<>(obfusChars);
        this.obfusLength = obfusLength;
    }

    /**
     * Returns the current obfuscated string in an exchangeable format.
     *
     * @see ObfusChar#getExchangeValue()
     */
    public String toExchangeFormat() {
        return toExchangeFormat(this);
    }

    /**
     * Returns the current obfuscated string as a fancy string.
     *
     * @see ObfusChar#getRepresentation(Representation)
     */
    public String toRepresentation(Representation representation) {
        return toRepresentation(this, representation);
    }

    public int length() {
        return obfusChars.size();
    }

    public List<ObfusChar> getObfusChars() {
        return obfusChars;
    }

    /**
     * Encrypts the given {@link ObfusString} with the given key.
     * @param key
     * @return
     *
     * @see ObfusChar#encrypt(byte)
     */
    public ObfusString encrypt(byte[] key) {

        int origLength = obfusChars.size();
        boolean isLengthObfuscable = origLength >= FIXED_OBFUS_MIN_LENGTH && origLength <= FIXED_OBFUS_MAX_LENGTH;

        ObfusString keyObfusString = EncryptUtil.keyToObfusString(key);
        int i = 0;
        List<ObfusChar> newObfusChars = new ArrayList<>(this.obfusChars);
        if (isLengthObfuscable) {
            int missingLength = FIXED_OBFUS_MAX_LENGTH - origLength;
            ObfusString expandPattern = EncryptUtil.keyToObfusString(EncryptUtil.generateRnd(missingLength));
            List<ObfusChar> generatedNoise = new ArrayList<>(expandPattern.getObfusChars());

            newObfusChars.addAll(generatedNoise);
            this.obfusLength = FIXED_OBFUS_MIN_LENGTH + encryptLength(origLength, key);
        }

        for (ObfusChar otherObfusChar : keyObfusString.getObfusChars()) {
            if (i >= newObfusChars.size()) {
                break;
            }
            ObfusChar origin = newObfusChars.get(i);

            if (origin.isEncryptable()) {
                newObfusChars.set(i, origin.encrypt((byte) otherObfusChar.ordinal()));
            }

            i++;
        }
        this.obfusChars = newObfusChars;

        return this;
    }

    private int encryptLength(int length, byte[] key) {
        return EncryptUtil.encryptIndex(length, FIXED_OBFUS_MAX_LENGTH, key);
    }

    private int decryptLength(int length, byte[] key) {
        return EncryptUtil.decryptIndex(length, FIXED_OBFUS_MAX_LENGTH, key);
    }

    /**
     * Decrypts the given {@link ObfusString} with the given key.
     * @param key
     * @return
     *
     * @see ObfusChar#decrypt(byte)
     */
    public ObfusString decrypt(byte[] key) {

        ObfusString keyObfusString = EncryptUtil.keyToObfusString(key);
        int i = 0;
        int originLength = obfusChars.size();
        if (isLengthObfuscated()) {
            originLength = FIXED_OBFUS_MIN_LENGTH + decryptLength(this.obfusLength, key);
            this.obfusLength = null;
        }
        List<ObfusChar> newObfusChars = new ArrayList<>(originLength);
        for (ObfusChar otherObfusChar : keyObfusString.getObfusChars()) {
            if (i >= originLength || i >= obfusChars.size()) {
                break;
            }
            ObfusChar origin = obfusChars.get(i);

            if (origin.isEncryptable()) {
                newObfusChars.add(i, origin.decrypt((byte) otherObfusChar.ordinal()));
            }

            i++;
        }
        this.obfusChars = newObfusChars;

        return this;
    }

    private boolean isLengthObfuscated() {
        return this.obfusLength != null;
    }

    public Integer getObfusLength() {
        return obfusLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObfusString)) return false;
        ObfusString that = (ObfusString) o;
        return Objects.equals(obfusChars, that.obfusChars);
    }

    @Override
    public int hashCode() {

        return Objects.hash(obfusChars);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (ObfusChar c: obfusChars) {
            sb.append(c.toString());
        }
        sb.append("-->l:");
        sb.append(obfusChars.size());
        if (obfusLength != null) {
            sb.append("/");
            sb.append(obfusLength);
            sb.append("?");
        }

        return sb.toString();
    }
}

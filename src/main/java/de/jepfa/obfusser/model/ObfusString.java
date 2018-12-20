package de.jepfa.obfusser.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.jepfa.obfusser.util.EncryptUtil;

/**
 * This class representens an obfuscated string.
 *
 * @see ObfusChar
 *
 * @author Jens Pfahl
 */
public class ObfusString {

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

        return new ObfusString(obfusChars);
    }

    /**
     * Creates an {@link ObfusString} from the given exchange format string.
     *
     * @param string
     * @return
     */
    public static ObfusString fromExchangeFormat(String string) {
        if (string == null) {
            return null;
        }
        List<ObfusChar> obfusChars = new ArrayList<>(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            obfusChars.add(ObfusChar.fromExchangeFormat(c));
        }

        return new ObfusString(obfusChars);
    }

    /**
     * Creates an exchange format string from the given {@link ObfusString}.
     *
     * @param obfusString
     * @return
     */
    protected static String toExchangeFormat(ObfusString obfusString) {
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
     * Creates a fancy representation string from the given {@link ObfusString}.
     *
     * @param obfusString
     * @return
     */
    protected static String toRepresentation(ObfusString obfusString, Representation representation) {
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

    /**
     * Copy constructor.
     *
     * @param other
     */
    public ObfusString(@NonNull ObfusString other) {
        this.obfusChars = new ArrayList<>(other.getObfusChars());
    }

    /**
     * Constructor.
     *
     * @param obfusChars
     * @see ObfusChar
     */
    public ObfusString(@NonNull List<ObfusChar> obfusChars) {
        this.obfusChars = obfusChars;
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
     * Replace a range of this {@link ObfusString} with placeholder markers.
     *
     * @param start
     * @param end
     */
    public void replaceWithPlaceholder(int start, int end) {
        for (int i = start; i < end && i < obfusChars.size(); i++) {
            obfusChars.set(i, ObfusChar.PLACEHOLDER);
        }
    }

    /**
     * Encrypts the given {@link ObfusString} with the given key.
     * @param key
     * @return
     *
     * @see ObfusChar#encrypt(byte)
     */
    public ObfusString encrypt(byte[] key) {
        ObfusString keyObfusString = EncryptUtil.keyToObfusString(key);
        int i = 0;
        for (ObfusChar otherObfusChar : keyObfusString.getObfusChars()) {
            if (i >= obfusChars.size()) {
                break;
            }
            ObfusChar origin = obfusChars.get(i);

            if (origin.isEncryptable()) {
                obfusChars.set(i, origin.encrypt((byte) otherObfusChar.ordinal()));
            }

            i++;
        }

        return this;
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
        for (ObfusChar otherObfusChar : keyObfusString.getObfusChars()) {
            if (i >= obfusChars.size()) {
                break;
            }
            ObfusChar origin = obfusChars.get(i);

            if (origin.isEncryptable()) {
                obfusChars.set(i, origin.decrypt((byte) otherObfusChar.ordinal()));
            }

            i++;
        }

        return this;
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
        return "ObfusString{" +
                "obfusChars=" + obfusChars +
                ", length=" + length() + "}";
    }
}

package de.jepfa.obfusser.model;

import de.jepfa.obfusser.ui.common.Debug;

public class CryptString implements CharSequence {

    public static CryptString of(String string) {
        if (string == null) {
            return null;
        }
        return new CryptString(string);
    }

    public static CryptString of(String string, String encrypted) {
        CryptString cryptString = new CryptString(string);
        cryptString.decrypted = string != null && encrypted != null
                && string.equals(encrypted);
        return cryptString;
    }

    public static String from(CryptString cryptString) {
        if (cryptString == null) {
            return null;
        }
        return cryptString.toString();
    }

    public static String toDebugString(CryptString cryptString) {
        if (cryptString == null) {
            return null;
        }
        if (Debug.INSTANCE.isDebug()) {
            return cryptString.toString() +
                    (cryptString.isDecrypted() ? "*" : "");
        }
        return cryptString.toString();
    }

    private String string;
    private boolean decrypted;

    private CryptString(String string) {
        this.string = string;
    }

    public boolean isDecrypted() {
        return decrypted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CryptString cryptString = (CryptString) o;

        return string.equals(cryptString.string);
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public int length() {
        return string.length();
    }

    @Override
    public char charAt(int index) {
        return string.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return string.subSequence(start, end);
    }

    public String toLowerCase() {
        return string.toLowerCase();
    }

    public String toUpperCase() {
        return string.toUpperCase();
    }

    @Override
    public String toString() {
        return string;
    }
}

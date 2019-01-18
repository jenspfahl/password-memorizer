package de.jepfa.obfusser.model;

import java.util.Arrays;

import de.jepfa.obfusser.util.Loop;

/**
 * A set of types used to obfuscate characters.
 * Each type is represented in a nice way.
 */
public enum ObfusChar {
    LOWER_CASE_CHAR('x', 0.4),
    UPPER_CASE_CHAR('X', 0.3),
    DIGIT('0', 0.2),
    SPECIAL_CHAR( '!', 0.1),
    ANY_CHAR('*', null),
    PLACEHOLDER('?', null),
    ;


    /**
     * Only the data set of this loop is able to encrypt/decrypt.
     * It only makes sense to encrypt weak data.
     */
    private static final Loop<ObfusChar> LOOP_ENCRYPTABLE_OBFUS_CHARS = new Loop<>(
            Arrays.asList(LOWER_CASE_CHAR, UPPER_CASE_CHAR, DIGIT, SPECIAL_CHAR));


    /**
     * Obfuscates the given char by the rules defined in this method.
     *
     * @param c
     * @return
     */
    public static ObfusChar obfuscate(char c) {
        if (Character.isDigit(c)) {
            return(ObfusChar.DIGIT);
        }
        else if (Character.isLowerCase(c)) {
            return(ObfusChar.LOWER_CASE_CHAR);
        }
        else if (Character.isUpperCase(c)) {
            return(ObfusChar.UPPER_CASE_CHAR);
        }
        else {
            return(ObfusChar.SPECIAL_CHAR);
        }
    }

    /**
     * Creates an {@link ObfusChar} from the given exchange format char.
     * @param c
     * @return
     */
    public static ObfusChar fromExchangeFormat(char c) {
        if (c == ObfusChar.DIGIT.getExchangeValue()) {
            return(ObfusChar.DIGIT);
        }
        else if (c == ObfusChar.LOWER_CASE_CHAR.getExchangeValue()) {
            return(ObfusChar.LOWER_CASE_CHAR);
        }
        else if (c == ObfusChar.UPPER_CASE_CHAR.getExchangeValue()) {
            return(ObfusChar.UPPER_CASE_CHAR);
        }
        else if (c == ObfusChar.SPECIAL_CHAR.getExchangeValue()) {
            return(ObfusChar.SPECIAL_CHAR);
        }
        else if (c == ObfusChar.ANY_CHAR.getExchangeValue()) {
            return(ObfusChar.ANY_CHAR);
        }
        else if (c == ObfusChar.PLACEHOLDER.getExchangeValue()) {
            return(ObfusChar.SPECIAL_CHAR); //TODO migration code
        }
        else {
            throw new IllegalStateException("Unknown exchange char: " + c);
        }
    }

    /**
     * Creates an {@link ObfusChar} from the given representation char.
     * @param c
     * @return
     */
    public static ObfusChar fromRepresentation(char c, Representation representation) {
        if (c == ObfusChar.DIGIT.getRepresentation(representation)) {
            return(ObfusChar.DIGIT);
        }
        else if (c == ObfusChar.LOWER_CASE_CHAR.getRepresentation(representation)) {
            return(ObfusChar.LOWER_CASE_CHAR);
        }
        else if (c == ObfusChar.UPPER_CASE_CHAR.getRepresentation(representation)) {
            return(ObfusChar.UPPER_CASE_CHAR);
        }
        else if (c == ObfusChar.SPECIAL_CHAR.getRepresentation(representation)) {
            return(ObfusChar.SPECIAL_CHAR);
        }
        else if (c == ObfusChar.ANY_CHAR.getRepresentation(representation)) {
            return(ObfusChar.ANY_CHAR);
        }
        else if (c == ObfusChar.PLACEHOLDER.getRepresentation(representation)) {
            return(ObfusChar.SPECIAL_CHAR); //TODO migration code
        }
        else {
            throw new IllegalStateException("Unknown exchange char: " + c);
        }
    }


    private char exchangeValue;
    private Double useLikelihood;


    ObfusChar( char exchangeValue, Double useLikelihood) {
        this.exchangeValue = exchangeValue;
        this.useLikelihood = useLikelihood;
    }

    /**
     * Returns the current obfuscated char as a fancy char.
     *
     * @return
     */
    public char getRepresentation(Representation representation) {
        return representation.getRepresentation(this);
     }

    /**
     * Returns the current obfuscated char in an exchangeable format.
     *
     * @return
     */
    public char getExchangeValue() {
        return exchangeValue;
    }

    /**
     * Indicates if the gicen {@link ObfusChar} is able to en-/decrypt with
     * {@link #encrypt(byte)} or {@link #decrypt(byte)}.
     * @return
     */
    public boolean isEncryptable() {
        return LOOP_ENCRYPTABLE_OBFUS_CHARS.applies(this);
    }

    /**
     * The approx. statistics use likelihood to create hard to guess encrypted chars.
     * @return
     */
    public Double getUseLikelihood() {
        return useLikelihood;
    }


    /**
     * Encrypts the given {@link ObfusChar} with the given key.
     *
     * @param key
     * @return
     */
    public ObfusChar encrypt(byte key) {
        return LOOP_ENCRYPTABLE_OBFUS_CHARS.forwards(this, key);
    }

    /**
     * Decrypts the given {@link ObfusChar} with the given key.
     *
     * @param key
     * @return
     */
    public ObfusChar decrypt(byte key) {
        return LOOP_ENCRYPTABLE_OBFUS_CHARS.backwards(this, key);
    }

    public String toExchangeFormat() {
        return new String(new char[]{getExchangeValue()});
    }

    public String toRepresentation(Representation representation) {
        return new String(new char[]{getRepresentation(representation)});
    }

    @Override
    public String toString() {
        return toExchangeFormat();
    }

}

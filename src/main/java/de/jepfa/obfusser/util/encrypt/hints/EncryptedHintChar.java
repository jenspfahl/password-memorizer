package de.jepfa.obfusser.util.encrypt.hints;

public class EncryptedHintChar extends HintChar {

    private static final int MAX_ROUND_TRIPS = 3;

    private int roundTrips;


    public static EncryptedHintChar ofDecrypted(char c) {
        return new EncryptedHintChar(c, 0);
    }

    /**
     *
     * @param encHintData format must be char + roundtrip (one digit), e.g. "x2"
     * @return
     */
    public static EncryptedHintChar ofEncrypted(String encHintData) {
        char[] chars = encHintData.toCharArray();
        char encHint = chars[0];
        int roundTrips = Integer.parseInt(String.valueOf(chars[1]));
        return new EncryptedHintChar(encHint, roundTrips);
    }

    public EncryptedHintChar(char hint, int roundTrips) {
        super(hint);
        this.roundTrips = roundTrips;
    }

    public int getRoundTrips() {
        return roundTrips;
    }

    public boolean doNext() {
        roundTrips++;
        return isGoNext() && getRoundTrips() < MAX_ROUND_TRIPS;
    }

    /**
     * @return format is char + roundtrip (one digit), e.g. "x2"
     */
    public String getHintStoreString() {
        return String.valueOf(getHint()) + roundTrips;
    }

    public void apply(HintChar hintChar) {
        if (hintChar != null) {
            setHint(hintChar.getHint());
            setGoNext(hintChar.isGoNext());
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EncryptedHintChar{");
        sb.append("hint='").append(getHint()).append('\'');
        sb.append(", goNext=").append(isGoNext());
        sb.append(", roundTrips=").append(roundTrips);
        sb.append('}');
        return sb.toString();
    }

}

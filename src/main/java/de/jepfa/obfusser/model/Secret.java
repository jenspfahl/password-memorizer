package de.jepfa.obfusser.model;

import java.util.concurrent.TimeUnit;

/**
 * This singleton holds the current user secret ({@link #digest}) delivered
 * by his password/pin/whatever.
 *
 * @author Jens Pfahl
 */
public class Secret {

    /**
     * Indicates user hasn't input secret yet.
     */
    public static final byte[] INVALID_DIGEST = {};

    /**
     * After this period of time of inactivity the secret is outdated.
     */
    private static final long SECRET_KEEP_VALID = TimeUnit.SECONDS.toMillis(60);

    private static Secret _instance = null;

    private volatile byte[] digest;
    private volatile long timestamp;


    public byte[] getDigest() {
        if (isOutdated()) {
            invalidate();
        }

        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
        renew();
    }

    public void invalidate() {
        digest = INVALID_DIGEST;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean hasDigest() {
        return digest != null && digest.length > 0;
    }

    public boolean isOutdated() {
        long age = System.currentTimeMillis() - timestamp;

        return age > SECRET_KEEP_VALID;
    }

    public void renew() {
        timestamp = System.currentTimeMillis();
    }

    public synchronized static Secret getOrCreate() {
        if (_instance == null) {
            _instance = new Secret();
        }

        return _instance;
    }

}

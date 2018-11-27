package de.jepfa.obfusser.model;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;

import de.jepfa.obfusser.util.EncryptUtil;

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
    private boolean secretDialogOpen;


    public byte[] getDigest() {
        if (isOutdated()) {
            setInvalidDigest();
        }

        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
        renew();
    }

    public void setInvalidDigest() {
        digest = INVALID_DIGEST;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isFilled() {
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

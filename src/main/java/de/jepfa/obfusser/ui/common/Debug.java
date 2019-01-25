package de.jepfa.obfusser.ui.common;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;

import de.jepfa.obfusser.BuildConfig;
import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.util.EncryptUtil;

public class Debug {

    private static boolean debug = false;

    public static void showDebugDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        StringBuilder sb = new StringBuilder();

        byte[] salt = SecureActivity.SecretChecker.getSalt(activity);
        addParam(sb, "AppSalt", endOfArrayToString(salt, 4) + ", l=" + arrayLength(salt));

        byte[] key = Secret.getOrCreate().getDigest();
        addParam(sb, "Key", endOfArrayToString(key, 4) + ", l=" + arrayLength(key));

        addParam(sb, "Key outdated", String.valueOf(Secret.getOrCreate().isOutdated()));

        addParam(sb, "Enc supported", String.valueOf(EncryptUtil.isPasswdEncryptionSupported()));
        addParam(sb, "Key stored", String.valueOf(SecureActivity.SecretChecker.isPasswordStored(activity)));
        addParam(sb, "Salt encrypted", String.valueOf(SecureActivity.SecretChecker.isSaltEncrypted(activity)));
        addParam(sb, "Enc with UUID", String.valueOf(SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity)));

        Drawable icon = activity.getApplicationInfo().loadIcon(activity.getPackageManager());
        builder.setTitle("Debug info")
                .setMessage(sb.toString())
                .setIcon(icon)
                .show();
    }

    public static synchronized void toggleDebug() {
        Debug.debug = !Debug.debug;
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG || debug == true;
    }

    private static void addParam(StringBuilder sb, String name, String value) {
        sb.append(name);
        sb.append(" = ");
        sb.append(value);
        sb.append(Constants.NL);
    }

    public static String endOfArrayToString(byte[] a, int count) {
        if (a == null)
            return "null";
        int iMin = Math.max(a.length - count, 0);
        int iMax = a.length - 1;
        if (iMax == -1)
            return "..]";

        StringBuilder b = new StringBuilder();
        b.append(".., ");
        for (int i = iMin; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    public static String arrayLength(byte[] a) {
        if (a == null) {
            return "n/a";
        }
        return String.valueOf(a.length);
    }
}

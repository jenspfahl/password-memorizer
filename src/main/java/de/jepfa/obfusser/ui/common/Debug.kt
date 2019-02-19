package de.jepfa.obfusser.ui.common

import android.app.Activity
import android.os.Build
import android.support.v7.app.AlertDialog
import android.util.Log

import de.jepfa.obfusser.BuildConfig
import de.jepfa.obfusser.Constants
import de.jepfa.obfusser.model.Secret
import de.jepfa.obfusser.ui.SecureActivity
import de.jepfa.obfusser.util.encrypt.EncryptUtil

object Debug {

    private var debug = BuildConfig.DEBUG

    val isDebug: Boolean
        get() = debug

    fun showDebugDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity)

        val sb = StringBuilder()

        try {
            val pInfo = activity.application.packageManager.getPackageInfo(activity.application.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                addParam(sb, "VersionCode", pInfo.longVersionCode.toString())
            }
            else {
                addParam(sb, "VersionCode", pInfo.versionCode.toString())
            }
            addParam(sb, "VersionName", pInfo.versionName)
        } catch (e: Exception) {
            Log.e("DEBUGINFO", "cannot get version code", e)
        }

        addParam(sb, "SdkVersion", Build.VERSION.SDK_INT.toString())

        val salt = SecureActivity.SecretChecker.getSalt(activity)
        addParam(sb, "AppSalt", endOfArrayToString(salt, 4) + ", len=" + arrayLength(salt))

        val key = Secret.getOrCreate().digest
        addParam(sb, "Key", endOfArrayToString(key, 4) + ", len=" + arrayLength(key))

        addParam(sb, "Key outdated", Secret.getOrCreate().isOutdated.toString())

        addParam(sb, "Enc supported", EncryptUtil.isPasswdEncryptionSupported().toString())
        addParam(sb, "Key stored", SecureActivity.SecretChecker.isPasswordStored(activity).toString())
        addParam(sb, "Salt encrypted", SecureActivity.SecretChecker.isSaltEncrypted(activity).toString())
        addParam(sb, "Enc with UUID", SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity).toString())

        val icon = activity.applicationInfo.loadIcon(activity.packageManager)
        builder.setTitle("Debug info")
                .setMessage(sb.toString())
                .setIcon(icon)
                .show()
    }

    @Synchronized
    fun toggleDebug() {
        Debug.debug = !Debug.debug
    }

    private fun addParam(sb: StringBuilder, name: String, value: String) {
        sb.append(name)
        sb.append(" = ")
        sb.append(value)
        sb.append(Constants.NL)
    }

    fun endOfArrayToString(a: ByteArray?, count: Int): String {
        if (a == null)
            return "null"
        val iMin = Math.max(a.size - count, 0)
        val iMax = a.size - 1
        if (iMax == -1)
            return "..]"

        val b = StringBuilder()
        b.append(".., ")
        var i = iMin
        while (true) {
            b.append(a[i].toInt())
            if (i == iMax)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    private fun arrayLength(a: ByteArray?): String {
        return a?.size?.toString() ?: "n/a"
    }
}

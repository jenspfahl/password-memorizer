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

        addParam(sb, "BuildTimestamp", Constants.SDF_DT_MEDIUM.format(BuildConfig.BUILD_TIME))
        addParam(sb, "BuildType", BuildConfig.BUILD_TYPE)
        addParam(sb, "SdkVersion", Build.VERSION.SDK_INT.toString())

        val salt = SecureActivity.SecretChecker.getSalt(activity)
        addParam(sb, "AppSalt", byteArrayChecksum(salt).toString() + ", len=" + arrayLength(salt))

        val key = Secret.getOrCreate().digest
        addParam(sb, "Key", byteArrayChecksum(key).toString() + ", len=" + arrayLength(key))

        addParam(sb, "Key outdated", Secret.getOrCreate().isOutdated.toString())

        addParam(sb, "Enc supported", EncryptUtil.isPasswdEncryptionSupported().toString())
        addParam(sb, "Key stored", SecureActivity.SecretChecker.isPasswordStored(activity).toString())
        addParam(sb, "Salt encrypted", SecureActivity.SecretChecker.isSaltEncrypted(activity).toString())
        addParam(sb, "Enc with UUID", SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity).toString())
        addParam(sb, "Uncrypted Strings", SecureActivity.SecretChecker.shouldDoCryptStrings(activity).toString())

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

    fun byteArrayChecksum(bytes: ByteArray?): Int {
        if (bytes == null)
            return 0

        var checkSum = 0
        for (b in bytes) {
            checkSum += b
        }
        return checkSum
    }

    private fun arrayLength(a: ByteArray?): String {
        return a?.size?.toString() ?: "n/a"
    }
}

package de.jepfa.obfusser.ui.common

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.View

import de.jepfa.obfusser.R
import de.jepfa.obfusser.ui.SecureActivity
import de.jepfa.obfusser.ui.settings.SettingsActivity
import de.jepfa.obfusser.ui.settings.fragments.SecurityPreferenceFragment

object Noogler {

    val PREF_DONT_NOOGLE_ENC_DATA = "de.jepfa.obfusser.dont_noogle_enc_data"
    val PREF_NOOGLE_ENC_DATA_COUNTER = "de.jepfa.obfusser.noogle_enc_data_counter"
    private val NOOGLE_DURATION = 10000

    fun noogleEncryptData(activity: Activity, view: View) {
        val defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity)
        val dontNoogle = isDontNoogle(defaultSharedPreferences)
        var noogleCounter = getNoogleCounter(defaultSharedPreferences)

        val edit = defaultSharedPreferences.edit()
        edit.putInt(PREF_NOOGLE_ENC_DATA_COUNTER, ++noogleCounter)
        edit.commit()

        if (!dontNoogle && !SecureActivity.SecretChecker.isPasswordCheckEnabled(activity)
                && isItTime(noogleCounter)) {

            val isOpenSettings = noogleCounter % 2 == 1
            val text = if (isOpenSettings) activity.getString(R.string.noogler_open_setting) else activity.getString(R.string.noogler_dismiss_forever)
            Snackbar.make(view, R.string.noogler_noogle_text, NOOGLE_DURATION)
                    .setAction(text) {
                        if (isOpenSettings) {
                            val intent = Intent(activity, SettingsActivity::class.java)
                            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SecurityPreferenceFragment::class.java.name)
                            activity.startActivity(intent)
                        } else {
                            val edit = defaultSharedPreferences.edit()
                            edit.putBoolean(PREF_DONT_NOOGLE_ENC_DATA, true)
                            edit.commit()
                        }
                    }
                    .show()
        }
    }

    fun getNoogleCounter(defaultSharedPreferences: SharedPreferences): Int {
        return defaultSharedPreferences
                .getInt(PREF_NOOGLE_ENC_DATA_COUNTER, 0)
    }

    fun isDontNoogle(defaultSharedPreferences: SharedPreferences): Boolean {
        return defaultSharedPreferences
                .getBoolean(PREF_DONT_NOOGLE_ENC_DATA, false)
    }

    private fun isItTime(noogleCounter: Int): Boolean {
        return noogleCounter % 19 == 3
    }


    fun resetPrefs(activity: Activity) {
        val defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity)
        val edit = defaultSharedPreferences.edit()
        edit.remove(PREF_DONT_NOOGLE_ENC_DATA)
        edit.remove(PREF_NOOGLE_ENC_DATA_COUNTER)
        edit.commit()
    }
}

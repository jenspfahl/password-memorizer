package de.jepfa.obfusser.util.encrypt

import android.support.v4.util.Pair
import android.util.Base64
import android.util.Log

import java.util.Arrays

object DbCrypt {

    private val KEY_DB_CRYPT = "key_db_crypt"
    private val CRYPTED_CONTENT_INDICATOR = "_vD+"
    private val BAS64_PAIR_DELIMITOR = ":"

    fun aesEncrypt(string: String?): String? {
        if (string == null) {
            return null
        }
        val data = EncryptUtil.encryptData(KEY_DB_CRYPT, string.toByteArray())
        if (data == null) {
            Log.e("AES-ENC", "Cannot encrypt given string")
            return string
        }
        return CRYPTED_CONTENT_INDICATOR + dataToString(data)
    }

    fun aesDecrypt(string: String?): String? {
        if (string == null) {
            return null
        }
        if (string.startsWith(CRYPTED_CONTENT_INDICATOR)) {
            val data = stringToData(string.substring(CRYPTED_CONTENT_INDICATOR.length))
            val dec = String(EncryptUtil.decryptData(KEY_DB_CRYPT, data)!!)
            if (dec == null) {
                Log.e("AES-DEC", "Cannot decrypt given string")
                return string
            }
            return dec
        }
        return string
    }

    private fun dataToString(data: Pair<ByteArray, ByteArray>): String {
        val first = Base64.encodeToString(data.first, Base64.NO_WRAP or Base64.NO_PADDING)
        val second = Base64.encodeToString(data.second, Base64.NO_WRAP or Base64.NO_PADDING)
        return first + BAS64_PAIR_DELIMITOR + second
    }

    private fun stringToData(string: String): Pair<ByteArray, ByteArray> {
        val splitted = string.split(BAS64_PAIR_DELIMITOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val first = Base64.decode(splitted[0], Base64.NO_WRAP or Base64.NO_PADDING)
        val second = Base64.decode(splitted[1], Base64.NO_WRAP or Base64.NO_PADDING)

        return Pair(first, second)
    }
}

package de.jepfa.obfusser.util

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.support.v4.content.CursorLoader
import android.util.Log

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter


/**
 * Utils to work with files.
 *
 * @author Jens Pfahl
 */
object FileUtil {

    /** Checks if external storage is available for read and write  */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    /** Checks if external storage is available to at least read  */
    val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    fun readFile(context: Context, uri: Uri): String? {
        //Read text from file
        val text = StringBuilder()

        try {
            context.contentResolver.openInputStream(uri)!!.use { `is` ->
                InputStreamReader(`is`).use { isr ->
                    BufferedReader(isr).use { br ->
                        var line: String?

                        do {
                            line = br.readLine()
                            if (line == null)
                                break;
                            text.append(line)
                            text.append('\n')
                        } while (true)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("READFILE", "Cannot read $uri", e)
            return null
        }

        return text.toString()
    }

    fun writeFile(context: Context, uri: Uri, content: String): Boolean {

        try {
            context.contentResolver.openOutputStream(uri)!!.use { os -> OutputStreamWriter(os).use { osw -> BufferedWriter(osw).use { bw -> bw.write(content) } } }
        } catch (e: IOException) {
            Log.e("READFILE", "Cannot read $uri", e)
            return false
        }

        return true
    }

    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }


}

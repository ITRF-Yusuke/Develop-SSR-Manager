/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

object FileUtils {

    val DEFAULT_CHARSET_NAME: String = "UTF-8"

    inline fun <reified T> loadJsonFromRaw(context: Context, resourceId: Int): T? {
        return loadJsonFromRaw(context, resourceId, DEFAULT_CHARSET_NAME)
    }

    inline fun <reified T> loadJsonFromRaw(context: Context, resourceId: Int, charsetName: String): T? {

        return try {
            context.resources.openRawResource(resourceId).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<T>() {}.type
                    Gson().fromJson<T>(reader, type)
                }
            }

        } catch (e: java.io.IOException) {
            e.printStackTrace()
            null
        }
    }

    fun loadTextFileFromRaw(context: Context, resourceId: Int): String? {
        return loadTextFileFromRaw(context, resourceId, DEFAULT_CHARSET_NAME)
    }

    fun loadTextFileFromRaw(context: Context, resourceId: Int, charsetName: String): String? {
        return try {
            val inputStream: InputStream = context.resources.openRawResource(resourceId)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName(charsetName))
        } catch (e: java.io.IOException) {
            e.printStackTrace()
            null
        }
    }

}
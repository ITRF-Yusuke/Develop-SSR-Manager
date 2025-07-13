/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.http

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.Callback
import okhttp3.Response
import org.kijitora.develop.ssr.BuildConfig
import org.kijitora.develop.ssr.R
import java.io.IOException

class JsonDownloader {
    companion object {

        fun getMasterData(context: Context, callback: (String?) -> Unit) {
            val client = OkHttpClient()
            val request: Request = Builder()
                .url(BuildConfig.MASTER_UNIT_DATA_URL)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, R.string.error_failed_get_master_data_json, Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        callback(response.body?.string())
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, R.string.message_succeeded_get_master_data_json, Toast.LENGTH_SHORT)
                                .show()
                        }

                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, R.string.error_failed_get_master_data_json, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            })

        }
    }


}
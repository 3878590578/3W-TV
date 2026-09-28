代码:
package com.oklocal.player

import android.content.Context
import org.json.JSONObject

object HistoryManager {

    private const val PREF =
        "player_history"

    private const val KEY =
        "history"

    private fun prefs(context: Context) =
        context.getSharedPreferences(
            PREF,
            Context.MODE_PRIVATE
        )

    fun savePosition(
        context: Context,
        uri: String,
        position: Long
    ) {

        val old =
            prefs(context)
                .getString(KEY, "{}")
                ?: "{}"

        val json =
            JSONObject(old)

        json.put(uri, position)

        prefs(context)
            .edit()
            .putString(KEY, json.toString())
            .apply()
    }

    fun getPosition(
        context: Context,
        uri: String
    ): Long {

        val old =
            prefs(context)
                .getString(KEY, "{}")
                ?: "{}"

        val json =
            JSONObject(old)

        return json.optLong(uri, 0L)
    }

    fun clear(
        context: Context,
        uri: String
    ) {

        val old =
            prefs(context)
                .getString(KEY, "{}")
                ?: "{}"

        val json =
            JSONObject(old)

        json.remove(uri)

        prefs(context)
            .edit()
            .putString(KEY, json.toString())
            .apply()
    }
}
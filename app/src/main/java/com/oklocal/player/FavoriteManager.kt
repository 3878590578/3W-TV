代码:
package com.oklocal.player

import android.content.Context

object FavoriteManager {

    private const val PREF =
        "favorites"

    private const val KEY =
        "favorite_list"

    private fun prefs(context: Context) =
        context.getSharedPreferences(
            PREF,
            Context.MODE_PRIVATE
        )

    fun isFavorite(
        context: Context,
        uri: String
    ): Boolean {

        return prefs(context)
            .getStringSet(
                KEY,
                emptySet()
            )
            ?.contains(uri)
            ?: false
    }

    fun toggle(
        context: Context,
        uri: String
    ): Boolean {

        val current =
            prefs(context)
                .getStringSet(
                    KEY,
                    emptySet()
                )
                ?.toMutableSet()
                ?: mutableSetOf()

        val result: Boolean

        if (current.contains(uri)) {
            current.remove(uri)
            result = false
        } else {
            current.add(uri)
            result = true
        }

        prefs(context)
            .edit()
            .putStringSet(KEY, current)
            .apply()

        return result
    }

    fun all(
        context: Context
    ): Set<String> {

        return prefs(context)
            .getStringSet(
                KEY,
                emptySet()
            )
            ?.toSet()
            ?: emptySet()
    }
}
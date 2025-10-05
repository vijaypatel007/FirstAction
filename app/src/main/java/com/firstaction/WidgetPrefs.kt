package com.firstaction

import android.content.Context
import android.content.SharedPreferences

object WidgetPrefs {
    private const val PREFS = "clock_widget_prefs"
    private const val KEY_LAST_HOUR_TENS = "last_hour_tens"
    private const val KEY_LAST_HOUR_ONES = "last_hour_ones"
    private const val KEY_LAST_MIN_TENS = "last_min_tens"
    private const val KEY_LAST_MIN_ONES = "last_min_ones"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveLastDigits(context: Context, hT: Int, hO: Int, mT: Int, mO: Int) {
        prefs(context).edit().apply {
            putInt(KEY_LAST_HOUR_TENS, hT)
            putInt(KEY_LAST_HOUR_ONES, hO)
            putInt(KEY_LAST_MIN_TENS, mT)
            putInt(KEY_LAST_MIN_ONES, mO)
            apply()
        }
    }

    fun getLastDigits(context: Context): IntArray {
        val p = prefs(context)
        return intArrayOf(
            p.getInt(KEY_LAST_HOUR_TENS, -1),
            p.getInt(KEY_LAST_HOUR_ONES, -1),
            p.getInt(KEY_LAST_MIN_TENS, -1),
            p.getInt(KEY_LAST_MIN_ONES, -1)
        )
    }
}

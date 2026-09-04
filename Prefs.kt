package com.gymlock.app

import android.content.Context

/**
 * Stores the user's chosen gym days (Calendar.SUNDAY..SATURDAY ints)
 * and the reminder hour/minute they picked in MainActivity.
 */
object Prefs {
    private const val FILE = "gym_lock_prefs"
    private const val KEY_DAYS = "days"
    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"
    private const val KEY_STREAK = "streak"
    private const val KEY_LAST_PHOTO = "last_photo_uri"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun saveDays(ctx: Context, days: Set<Int>) {
        sp(ctx).edit().putStringSet(KEY_DAYS, days.map { it.toString() }.toSet()).apply()
    }

    fun getDays(ctx: Context): Set<Int> =
        sp(ctx).getStringSet(KEY_DAYS, emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()

    fun saveTime(ctx: Context, hour: Int, minute: Int) {
        sp(ctx).edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
    }

    fun getHour(ctx: Context) = sp(ctx).getInt(KEY_HOUR, 18)
    fun getMinute(ctx: Context) = sp(ctx).getInt(KEY_MINUTE, 0)

    fun incrementStreak(ctx: Context): Int {
        val s = sp(ctx).getInt(KEY_STREAK, 0) + 1
        sp(ctx).edit().putInt(KEY_STREAK, s).apply()
        return s
    }

    fun getStreak(ctx: Context) = sp(ctx).getInt(KEY_STREAK, 0)

    fun saveLastPhoto(ctx: Context, uri: String) {
        sp(ctx).edit().putString(KEY_LAST_PHOTO, uri).apply()
    }
}

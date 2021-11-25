package com.developers.noteappktorserver.utils

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.*


object Constants {
    val ACTION_LOGIN_FRAGMENT_AFTER_LOGOUT: String="ACTION_LOGIN_FRAGMENT_AFTER_LOGOUT"
    const val TAG = "GAMALRAGAB"
    const val REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS=123
    const val USERS_INFO_FILE: String = "USER_INFO"
    const val BASE_URL = "http://d44a-197-38-125-238.ngrok.io/v1/"
    val USER_TOKEN = stringPreferencesKey("USER_TOKEN")
    val USER_EMAIL = stringPreferencesKey("USER_EMAIL")
    val USER_PASSWORD = stringPreferencesKey("USER_PASSWORD")
    const val IMAGE_URL =
        "data:image/jpeg;base64,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"


    const val MIN_TITLENOTE_LENGTH=3
    const val MAX_TITLENOTE_LENGTH=50

    const val MIN_SUBTITLE_LENGTH=3
    const val MAX_SUBTITLE_LENGTH=40
    const val SEARCH_TIME_DELAY= 500L



    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    fun getTimeAgo(time: Long, ctx: Context?): String? {
        var time = time
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now: Long = Date().time
        if (time > now || time <= 0) {
            return null
        }

        // TODO: localize
        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            " ${diff / MINUTE_MILLIS}  minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            " ${diff / HOUR_MILLIS} hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            "yesterday"
        } else {
            "${diff / DAY_MILLIS} days ago"
        }
    }

    private var suffixes: NavigableMap<Long, String> = TreeMap()

    fun init() {
        suffixes[1_000L] = "k"
        suffixes[1_000_000L] = "M"
        suffixes[1_000_000_000L] = "G"
        suffixes[1_000_000_000_000L] = "T"
        suffixes[1_000_000_000_000_000L] = "P"
        suffixes[1_000_000_000_000_000_000L] = "E"
    }

    fun format(value: Long): String {
        init()
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1)
        if (value < 0) return "-" + format(-value)
        if (value < 1000) return java.lang.Long.toString(value) //deal with easy case
        val e = suffixes.floorEntry(value)
        val divideBy = e.key
        val suffix = e.value
        val truncated = value / (divideBy / 10) //the number part of the output times 10
        val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
    }

}
package xyz.sentrionic.harmony.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {

    companion object {

        private const val SECOND_MILLIS: Long = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private const val DAY_MILLIS = 24 * HOUR_MILLIS

        // dates from server look like this: "2019-07-23T03:28:01.406944Z"
        fun convertServerStringDateToLong(sd: String): Long {
            val stringDate = sd.replace("T", " ")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            try {
                return sdf.parse(stringDate).time
            } catch (e: Exception) {
                throw Exception(e)
            }
        }

        fun convertLongToStringDate(time: Long): String {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            val now = System.currentTimeMillis()

            if (time > now || time <= 0) {
                return ""
            }

            val diff = now - time

            return when {
                diff < MINUTE_MILLIS -> "just now"
                diff < 2 * MINUTE_MILLIS -> "a minute ago"
                diff < 50 * MINUTE_MILLIS -> (diff / MINUTE_MILLIS).toString() + " minutes ago"
                diff < 90 * MINUTE_MILLIS -> "1 hour ago"
                diff < 24 * HOUR_MILLIS -> (diff / HOUR_MILLIS).toString() + " hours ago"
                diff < 48 * HOUR_MILLIS -> "yesterday"
                diff < 30 * DAY_MILLIS -> (diff / DAY_MILLIS).toString() + " days ago"
                else -> sdf.format(Date(time))
            }
        }
    }
}
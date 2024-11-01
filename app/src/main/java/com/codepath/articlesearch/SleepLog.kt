package com.codepath.articlesearch

data class SleepLog(
    val date_of_night: String?,
    val hours_slept: String?,
    val sleep_rating: String?,
    val note: String?
) : java.io.Serializable
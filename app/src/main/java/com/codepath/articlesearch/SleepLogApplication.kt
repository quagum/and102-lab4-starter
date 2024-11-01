package com.codepath.articlesearch

import android.app.Application

class SleepLogApplication : Application() {
    val db by lazy { AppDatabase.getInstance(this) }
}
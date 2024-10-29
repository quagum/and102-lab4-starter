package com.codepath.articlesearch

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Switch

class SettingsActivity : AppCompatActivity() {
    private lateinit var cacheSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        cacheSwitch = findViewById(R.id.cacheSwitch)

        // Load saved preference
        val isCachingEnabled = sharedPreferences.getBoolean("cache_data", true)
        cacheSwitch.isChecked = isCachingEnabled

        // Set listener on the switch to save preference
        cacheSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("cache_data", isChecked)
                apply()
            }
        }
    }
}

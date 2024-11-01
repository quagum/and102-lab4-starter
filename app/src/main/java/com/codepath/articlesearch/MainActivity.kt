package com.codepath.articlesearch

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.articlesearch.databinding.ActivityMainBinding
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = "MainActivity/"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sleepLogAdapter: SleepLogAdapter
    private lateinit var sleepLogRecyclerView:  RecyclerView
    private lateinit var yesterday_date: String

    private val logs = mutableListOf<SleepLog>()
    private var average_hours = ""
    private var average_rating = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchLogsFromDatabase() // Fetch data in a coroutine

        //setup recycler view
        sleepLogRecyclerView = findViewById(R.id.sleepLogs)
        sleepLogAdapter = SleepLogAdapter(this, logs)
        sleepLogRecyclerView.adapter = sleepLogAdapter
        sleepLogRecyclerView.layoutManager = LinearLayoutManager(this).also {
            val dividerItemDecoration = DividerItemDecoration(this, it.orientation)
            sleepLogRecyclerView.addItemDecoration(dividerItemDecoration)
        }


        // Initialize date input view
        val dateInput = findViewById<EditText>(R.id.dateInput)

        // Set default date to the previous night
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        yesterday_date = dateFormat.format(calendar.time)
        dateInput.setText(yesterday_date)

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            submitLogEntry()
        }
    }

    private fun submitLogEntry() {
        // Read slider values on the main thread
        var date = findViewById<EditText>(R.id.dateInput).text.toString()
        val hours = findViewById<Slider>(R.id.hoursOfSleepSlider).value.toString()
        val quality =  findViewById<Slider>(R.id.qualityOfSleepSlider).value.toString()
        var note = findViewById<EditText>(R.id.notesInput).text.toString()

        // Validate inputs
        if (date.isEmpty()) {
           date = yesterday_date
        }
        if (note.isEmpty()) {
            note = "no notes"
        }

        // Create a new log entry
        val log = SleepLog(date, hours, quality, note)
        logs.add(log)
        sleepLogAdapter.notifyItemInserted(logs.size - 1)

        // Insert the log into the database
        lifecycleScope.launch(Dispatchers.IO) {
            (application as SleepLogApplication).db.sleepLogDao().insertAll(listOf(SleepLogEntity(
                date_of_night = date,
                hours_slept = hours,
                sleep_rating = quality,
                note = note
            )))
            val average_hours = (application as SleepLogApplication).db.sleepLogDao().getAverageHours()?.toString() ?: "N/A"
            val average_rating = (application as SleepLogApplication).db.sleepLogDao().getAverageRating()?.toString() ?: "N/A"
            //fill in averages view
            val averageTextView = findViewById<TextView>(R.id.averageSleepData)
            lifecycleScope.launch(Dispatchers.Main) {
                averageTextView.text = "Average Hours: $average_hours\nAverage Quality: $average_rating"
            }

        }

        // Clear input fields
        clearInputFields()
    }

    private fun clearInputFields() {
        binding.dateInput.setText(yesterday_date)
        binding.hoursOfSleepSlider.value = 0f
        binding.qualityOfSleepSlider.value = 1f
        binding.notesInput.text.clear()
    }

    private fun fetchLogsFromDatabase() {
        lifecycleScope.launch {
            (application as SleepLogApplication).db.sleepLogDao().getAll().collect { databaseList ->
                val mappedList = databaseList.map { entity ->
                    SleepLog(
                        entity.date_of_night ?: "No Date",
                        entity.hours_slept ?: "0",
                        entity.sleep_rating ?: "1",
                        entity.note ?: "No Notes"
                    )
                }
                // Clear and add new logs
                logs.clear()
                logs.addAll(mappedList)
                sleepLogAdapter.notifyDataSetChanged() // Notify adapter of changes
            }
        }
    }
}

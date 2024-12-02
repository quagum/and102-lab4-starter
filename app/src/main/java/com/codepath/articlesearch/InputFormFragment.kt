package com.codepath.articlesearch

import SleepLogViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InputFormFragment : Fragment() {
    private lateinit var yesterdayDate: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize date input view
        val dateInput = view.findViewById<EditText>(R.id.dateInput)

        // Set default date to the previous night
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        yesterdayDate = dateFormat.format(calendar.time)
        dateInput.setText(yesterdayDate)

        view.findViewById<Button>(R.id.submitButton).setOnClickListener {
            submitLogEntry(view)
        }
    }

    private fun submitLogEntry(view: View) {
        // Read slider values on the main thread
        var date = view.findViewById<EditText>(R.id.dateInput).text.toString()
        val hours = view.findViewById<Slider>(R.id.hoursOfSleepSlider).value.toString()
        val quality = view.findViewById<Slider>(R.id.qualityOfSleepSlider).value.toString()
        var note = view.findViewById<EditText>(R.id.notesInput).text.toString()

        // Validate inputs
        if (date.isEmpty()) {
            date = yesterdayDate
        }
        if (note.isEmpty()) {
            note = "no notes"
        }

        // Create a new log entry
        val log = SleepLog(date, hours, quality, note)

        // Insert the log into the database
        lifecycleScope.launch(Dispatchers.IO) {
            (requireActivity().application as SleepLogApplication).db.sleepLogDao().insertAll(
                listOf(
                    SleepLogEntity(
                        date_of_night = date,
                        hours_slept = hours,
                        sleep_rating = quality,
                        note = note
                    )
                )
            )
        }

        // Clear input fields
        clearInputFields(view)
    }

    private fun clearInputFields(view: View) {
        view.findViewById<EditText>(R.id.dateInput).setText(yesterdayDate)
        view.findViewById<Slider>(R.id.hoursOfSleepSlider).value = 0f
        view.findViewById<Slider>(R.id.qualityOfSleepSlider).value = 1f
        view.findViewById<EditText>(R.id.notesInput).text.clear()
    }
}

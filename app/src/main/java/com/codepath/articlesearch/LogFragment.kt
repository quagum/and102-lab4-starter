package com.codepath.articlesearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
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

class LogFragment : Fragment() {
    private lateinit var sleepLogAdapter: SleepLogAdapter
    private lateinit var sleepLogRecyclerView: RecyclerView
    private lateinit var yesterdayDate: String
    private val logs = mutableListOf<SleepLog>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        sleepLogRecyclerView = view.findViewById(R.id.sleepLogs)
        sleepLogAdapter = SleepLogAdapter(requireContext(), logs)
        sleepLogRecyclerView.adapter = sleepLogAdapter
        sleepLogRecyclerView.layoutManager = LinearLayoutManager(requireContext()).also {
            val dividerItemDecoration = DividerItemDecoration(requireContext(), it.orientation)
            sleepLogRecyclerView.addItemDecoration(dividerItemDecoration)
        }

        // Fetch existing logs from the database
        fetchLogsFromDatabase()
    }

    private fun fetchLogsFromDatabase() {
        lifecycleScope.launch {
            (requireActivity().application as SleepLogApplication).db.sleepLogDao().getAll().collect { databaseList ->
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

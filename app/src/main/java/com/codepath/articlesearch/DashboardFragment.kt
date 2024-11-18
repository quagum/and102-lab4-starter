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

class DashboardFragment : Fragment() {
    private lateinit var sleepLogAdapter: SleepLogAdapter
    private lateinit var sleepLogRecyclerView: RecyclerView
    private val logs = mutableListOf<SleepLog>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the averages
        lifecycleScope.launch(Dispatchers.IO) {
            val averageHours = (requireActivity().application as SleepLogApplication).db.sleepLogDao().getAverageHours()?.toString() ?: "N/A"
            val roundedAverageHours = roundToNearestTenth(averageHours.toDouble());
            val averageRating = (requireActivity().application as SleepLogApplication).db.sleepLogDao().getAverageRating()?.toString() ?: "N/A"
            val roundedAverageRating = roundToNearestTenth(averageRating.toDouble());
            // Update the averages view on the main thread
            lifecycleScope.launch(Dispatchers.Main) {
                view.findViewById<TextView>(R.id.averageSleepData).text = "Average Hours: $roundedAverageHours/24\nAverage Quality: $roundedAverageRating/10"
            }
        }
    }

    fun roundToNearestTenth(number: Double): Double {
        return String.format("%.1f", number).toDouble()
    }

}

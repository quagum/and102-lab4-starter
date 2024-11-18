package com.codepath.articlesearch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

const val LOG_EXTRA = "LOG_EXTRA"
private const val TAG = "SleepLogAdapter"

class SleepLogAdapter(private val context: Context, private val logs: List<SleepLog>) :
    RecyclerView.Adapter<SleepLogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.sleep_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.bind(log)
    }

    override fun getItemCount() = logs.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView = itemView.findViewById<TextView>(R.id.logDate)
        private val hoursOfSleepTextView = itemView.findViewById<TextView>(R.id.logHoursOfSleep)
        private val qualityOfSleepTextView = itemView.findViewById<TextView>(R.id.logQualityOfSleep)
        private val notesTextView = itemView.findViewById<TextView>(R.id.logNotes)

        fun bind(log: SleepLog) {
            dateTextView.text = log.date_of_night
            hoursOfSleepTextView.text = log.hours_slept
            qualityOfSleepTextView.text = log.sleep_rating
            notesTextView.text = log.note
        }
    }


}

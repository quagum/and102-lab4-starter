import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codepath.articlesearch.SleepLog

class SleepLogViewModel : ViewModel() {
    // LiveData to hold the list of sleep logs
    val sleepLogs = MutableLiveData<MutableList<SleepLog>>(mutableListOf())

    // Add a new sleep log
    fun addSleepLog(log: SleepLog) {
        val currentLogs = sleepLogs.value ?: mutableListOf()
        currentLogs.add(log)
        sleepLogs.value = currentLogs
    }
}

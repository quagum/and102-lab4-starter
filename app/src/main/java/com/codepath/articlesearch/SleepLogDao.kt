package com.codepath.articlesearch

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "date_of_night") val date_of_night: String?,
    @ColumnInfo(name = "hours_slept") val hours_slept: String?,
    @ColumnInfo(name = "sleep_rating") val sleep_rating: String?,
    @ColumnInfo(name = "note") val note: String?
)

@Dao
interface SleepLogDao {
    @Query("SELECT * FROM sleep_logs ORDER BY id DESC")
    fun getAll(): Flow<List<SleepLogEntity>>

    @Insert
    fun insertAll(articles: List<SleepLogEntity>)

    @Query("DELETE FROM sleep_logs")
    fun deleteAll()

    @Query("SELECT AVG(hours_slept) FROM sleep_logs")
    fun getAverageHours(): Double?

    @Query("SELECT AVG(sleep_rating) FROM sleep_logs")
    fun getAverageRating(): Double?
}


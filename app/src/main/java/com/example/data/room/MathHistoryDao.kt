package com.example.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MathHistoryDao {
    @Query("SELECT * FROM math_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<MathHistoryEntity>>

    @Query("SELECT * FROM math_history WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarks(): Flow<List<MathHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MathHistoryEntity): Long

    @Delete
    suspend fun delete(entity: MathHistoryEntity)

    @Query("UPDATE math_history SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmark(id: Long, isBookmarked: Boolean)

    @Query("DELETE FROM math_history")
    suspend fun clearHistory()
}

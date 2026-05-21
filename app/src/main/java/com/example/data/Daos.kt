package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds")
    fun getAllFeeds(): Flow<List<Feed>>

    @Query("SELECT * FROM feeds")
    suspend fun getAllFeedsSync(): List<Feed>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feed: Feed)
    
    @Query("DELETE FROM feeds WHERE id = :id")
    suspend fun delete(id: Int)
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news ORDER BY id DESC")
    fun getAllNews(): Flow<List<News>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news: News): Long
    
    @Query("SELECT * FROM news WHERE link = :link LIMIT 1")
    suspend fun getNewsByLink(link: String): News?
    
    @Query("UPDATE news SET isNotified = 1 WHERE id = :id")
    suspend fun markAsNotified(id: Int)

    @Query("UPDATE news SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)
}

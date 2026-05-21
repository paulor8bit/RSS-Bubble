package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeds")
data class Feed(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String = ""
)

@Entity(tableName = "news")
data class News(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val feedId: Int,
    val title: String,
    val link: String,
    val pubDate: String,
    val summary: String? = null,
    val isRead: Boolean = false,
    val isNotified: Boolean = false
)

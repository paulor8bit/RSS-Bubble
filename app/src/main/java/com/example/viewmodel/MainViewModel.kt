package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.AppDatabase
import com.example.data.Feed
import com.example.worker.RssFetchWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val feedDao = database.feedDao()
    private val newsDao = database.newsDao()

    val feeds: StateFlow<List<Feed>> = feedDao.getAllFeeds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val news = newsDao.getAllNews()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        setupWorkManager()
    }

    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val periodicRequest = PeriodicWorkRequestBuilder<RssFetchWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            "rss_fetch_work",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    fun syncNow() {
        val request = androidx.work.OneTimeWorkRequestBuilder<RssFetchWorker>().build()
        WorkManager.getInstance(getApplication()).enqueue(request)
    }

    fun addFeed(url: String, title: String = "") {
        viewModelScope.launch {
            feedDao.insert(Feed(url = url, title = title))
        }
    }

    fun deleteFeed(id: Int) {
        viewModelScope.launch {
            feedDao.delete(id)
        }
    }
}

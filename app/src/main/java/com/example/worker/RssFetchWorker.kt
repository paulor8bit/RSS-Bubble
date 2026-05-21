package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.api.summarizeNewsContext
import com.example.data.AppDatabase
import com.example.data.News
import com.example.rss.RssParser
import com.example.service.BubbleManager

class RssFetchWorker(
    appContext: Context, 
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val feeds = database.feedDao().getAllFeedsSync()
        
        for (feed in feeds) {
            val rssResult = RssParser.fetchFeed(feed.url)
            if (rssResult != null && rssResult.items.isNotEmpty()) {
                val latestItem = rssResult.items.first()
                
                val existingNews = database.newsDao().getNewsByLink(latestItem.link)
                if (existingNews == null) {
                    val summary = summarizeNewsContext(latestItem.title, latestItem.description)
                    val news = News(
                        feedId = feed.id,
                        title = latestItem.title,
                        link = latestItem.link,
                        pubDate = latestItem.pubDate,
                        summary = summary
                    )
                    val id = database.newsDao().insert(news)
                    
                    // Show Floating Bubble
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        BubbleManager.showBubble(applicationContext, news.title, summary)
                    }
                    database.newsDao().markAsNotified(id.toInt())
                }
            }
        }
        return Result.success()
    }
}

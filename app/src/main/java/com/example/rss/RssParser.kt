package com.example.rss

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RssResult(val title: String, val items: List<RssItem>)
data class RssItem(val title: String, val link: String, val description: String, val pubDate: String)

object RssParser {
    suspend fun fetchFeed(urlString: String): RssResult? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 10000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.connect()
            
            val stream = conn.inputStream
            val result = parseXml(stream)
            stream.close()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseXml(inputStream: InputStream): RssResult? {
        try {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            
            var feedTitle = "RSS Feed"
            val items = mutableListOf<RssItem>()
            
            var insideItem = false
            var eventType = parser.eventType
            
            var currentTitle = ""
            var currentLink = ""
            var currentDesc = ""
            var currentPubDate = ""
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true)) {
                            insideItem = true
                            currentTitle = ""
                            currentLink = ""
                            currentDesc = ""
                            currentPubDate = ""
                        } else if (name.equals("title", ignoreCase = true)) {
                            val text = parser.nextText()
                            if (insideItem) currentTitle = text else feedTitle = text
                        } else if (name.equals("link", ignoreCase = true)) {
                            var text = ""
                            if (parser.attributeCount > 0 && parser.getAttributeValue(null, "href") != null) {
                                text = parser.getAttributeValue(null, "href")
                            } else {
                                text = parser.nextText()
                            }
                            if (insideItem) currentLink = text
                        } else if (name.equals("description", ignoreCase = true) || name.equals("summary", ignoreCase = true) || name.equals("content", ignoreCase = true)) {
                            if (insideItem) currentDesc = parser.nextText()
                        } else if (name.equals("pubDate", ignoreCase = true) || name.equals("published", ignoreCase = true) || name.equals("updated", ignoreCase = true)) {
                            if (insideItem) currentPubDate = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true)) {
                            insideItem = false
                            items.add(RssItem(currentTitle, currentLink, currentDesc, currentPubDate))
                        }
                    }
                }
                eventType = parser.next()
            }
            return RssResult(feedTitle, items)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

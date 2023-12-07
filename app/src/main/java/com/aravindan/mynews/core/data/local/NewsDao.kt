package com.aravindan.mynews.core.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aravindan.mynews.feature_news.domain.model.News

@Dao
interface NewsDao {

    @Upsert
    suspend fun upsertNews(news: List<News>)

    @Query("SELECT COUNT(id) FROM news")
    suspend fun getCount(): Int

    @Query("SELECT * FROM news ORDER BY publishedAt DESC")
    suspend fun getNewsList(): List<News>

    @Query("SELECT * FROM news WHERE title LIKE :query")
    fun getNewsQuery(query: String): List<News>

    @Query("DELETE FROM news")
    suspend fun clearAllNews()
}
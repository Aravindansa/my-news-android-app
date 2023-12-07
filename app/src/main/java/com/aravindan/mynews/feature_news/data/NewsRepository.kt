package com.aravindan.mynews.feature_news.data

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.aravindan.mynews.core.data.local.AppDatabase
import com.aravindan.mynews.core.data.remote.Api
import com.aravindan.mynews.feature_news.domain.NewsPagingSource
import com.aravindan.mynews.feature_news.domain.model.News
import kotlinx.coroutines.flow.Flow

class NewsRepository (val app:Application,val api:Api,val appDatabase: AppDatabase){
    companion object {
        const val NETWORK_PAGE_SIZE = 10
    }

    fun getNews(query: String?=null): Flow<PagingData<News>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false),
            pagingSourceFactory = { NewsPagingSource(appDatabase,api,app,query) }
        ).flow
    }





}
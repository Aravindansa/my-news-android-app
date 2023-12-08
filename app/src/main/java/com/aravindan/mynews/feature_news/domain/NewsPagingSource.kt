package com.aravindan.mynews.feature_news.domain

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.aravindan.mynews.R
import com.aravindan.mynews.core.data.local.AppDatabase
import com.aravindan.mynews.core.data.remote.Api
import com.aravindan.mynews.core.util.MyUtil
import com.aravindan.mynews.feature_news.domain.model.News


private const val STARTING_OFFSET = 0
class NewsPagingSource(val database: AppDatabase, val api: Api, val context: Context, val query: String?=null): PagingSource<Int, News>() {
    override fun getRefreshKey(state: PagingState<Int, News>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, News> {
        return try {
            val dbNewsCount=database.appDao().getCount()
            if (query?.trim().isNullOrEmpty() && MyUtil.isOnline(context)){
                val offset=params.key?: STARTING_OFFSET
                val news=api.getNews(offset = offset)
                if (news.isSuccess){
                    val newsList=news.getOrNull()?.results
                    if (!newsList.isNullOrEmpty()){
                        database.withTransaction {
                            if (offset==0)
                                database.appDao().clearAllNews()
                            database.appDao().upsertNews(newsList)
                        }
                    }
                    LoadResult.Page(
                        data = newsList?: emptyList(),
                        prevKey = null,
                        nextKey = if (newsList.isNullOrEmpty()) null else offset + 10
                    )
                }else{
                    LoadResult.Error(Throwable(context.getString(R.string.something_went_wrong)))
                }
            }else if (dbNewsCount<1){
                LoadResult.Error(Throwable(context.getString(R.string.internet_connection_alert)))
            }
            else{
                var news=database.appDao().getNewsList()
                if (!query.isNullOrEmpty()){
                    news=news.filter {
                        it.title.lowercase().contains(query.lowercase())
                    }
                }
                LoadResult.Page(
                    data = news,
                    prevKey = null,
                    nextKey =  null
                )
            }
        }catch (e:Exception){
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

}
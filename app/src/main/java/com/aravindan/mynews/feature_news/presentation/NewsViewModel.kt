package com.aravindan.mynews.feature_news.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aravindan.mynews.feature_news.data.NewsRepository
import com.aravindan.mynews.feature_news.domain.model.News
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) :ViewModel(){
    var newsList: LiveData<PagingData<News>>
     val currentQuery = MutableLiveData<String?>()
    init {
        currentQuery.value = ""
        newsList= currentQuery.switchMap {
            repository.getNews(it).asLiveData().cachedIn(viewModelScope)
        }
    }
    fun search(query: String?){
        currentQuery.value=query
    }
}
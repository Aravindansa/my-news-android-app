package com.sa.mynews.feature_news.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import com.sa.mynews.core.util.collectLatestLifecycleFlow
import com.sa.mynews.databinding.FragmentNewsBinding
import com.sa.mynews.feature_news.domain.NewsListLoadStateAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewsFragment : Fragment() {
    private val viewModel:NewsViewModel by viewModels()
    private var binding: FragmentNewsBinding? = null
    private var newsAdapter:NewsAdapter?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFun()
        observeData()
    }
    fun scrollNewsToTop(){
        binding?.recyclerNews?.scrollToPosition(0)
    }

    private fun observeData() {
        viewModel.newsList.observe(viewLifecycleOwner){
            newsAdapter?.submitData(lifecycle,it)
        }
        collectLatestLifecycleFlow(newsAdapter!!.loadStateFlow){
            binding?.loader?.apply {
                if(it.refresh is LoadState.Loading){
                    loaderContent.isVisible=true
                    progressBar.isVisible=true
                    retryButton.isVisible=false
                    errorMsg.isVisible=false
                    binding!!.recyclerNews.isVisible=false
                }else if (it.refresh is LoadState.Error){
                    binding!!.recyclerNews.isVisible=false
                    binding!!.searchView.isVisible=false
                    errorMsg.text = (it.refresh as LoadState.Error).error.localizedMessage
                    loaderContent.isVisible=true
                    progressBar.isVisible=false
                    retryButton.isVisible=true
                    errorMsg.isVisible=true
                }else{
                    if (!binding!!.searchView.isVisible)
                        binding!!.searchView.isVisible=true
                    binding!!.recyclerNews.isVisible=true
                    loaderContent.isVisible=false
                }
            }
        }
    }

    private fun initFun() {
        newsAdapter= NewsAdapter(requireContext()){
            startActivity(Intent(requireContext(),NewsDetailActivity::class.java)
                .putExtra(NewsDetailActivity.URL,it.url)
                .putExtra(NewsDetailActivity.TITLE,it.title))
        }
        binding?.apply {
            recyclerNews.apply {
                itemAnimator=null
                adapter=newsAdapter?.withLoadStateFooter(
                    footer =  NewsListLoadStateAdapter{newsAdapter?.retry()}
                )
            }
            loader.retryButton.setOnClickListener {
                newsAdapter?.refresh()
            }
            searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean =false
                override fun onQueryTextChange(newText: String?): Boolean {
                    if(searchView.hasFocus())
                        viewModel.search(newText)

                    return false
                }

            })
            swipeRefresh.setOnRefreshListener {
               swipeRefresh.isRefreshing=false
                searchView.setQuery("",false)
                viewModel.currentQuery.value=""
                newsAdapter?.refresh()
            }
        }

    }

}
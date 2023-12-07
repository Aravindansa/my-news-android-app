package com.aravindan.mynews.feature_news.domain

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aravindan.mynews.databinding.ItemNewsLoadStateBinding

class NewsListLoadStateAdapter(val retry:()->Unit):
    LoadStateAdapter<NewsListLoadStateAdapter.LoadStateViewHolder>() {
    inner class LoadStateViewHolder(val binding: ItemNewsLoadStateBinding): RecyclerView.ViewHolder(binding.root){
        fun bindData(loadState: LoadState){
            binding.retryButton.setOnClickListener {
                retry.invoke()
            }
            if (loadState is LoadState.Error)
                binding.errorMsg.text = loadState.error.localizedMessage

            binding.progressBar.isVisible=loadState is LoadState.Loading
            binding.retryButton.isVisible=loadState is LoadState.Error
            binding.errorMsg.isVisible=loadState is LoadState.Error
        }
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bindData(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        return LoadStateViewHolder(ItemNewsLoadStateBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }


}

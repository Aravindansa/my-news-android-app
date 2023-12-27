package com.sa.mynews.feature_news.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sa.mynews.core.util.loadImage
import com.sa.mynews.core.util.setResizableText
import com.sa.mynews.databinding.ItemNewsBinding
import com.sa.mynews.feature_news.domain.model.News


class NewsAdapter (private val context: Context, private val onclick:(News)->Unit): PagingDataAdapter<News, NewsAdapter.ViewHolder>(DIFF_UTIL){

    companion object{
        private val DIFF_UTIL=object :DiffUtil.ItemCallback<News>(){
            override fun areItemsTheSame(oldItem: News, newItem: News): Boolean =oldItem.id==newItem.id
            override fun areContentsTheSame(oldItem: News, newItem: News): Boolean =false

        }
    }
    inner class ViewHolder(val binding:ItemNewsBinding):RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bindData(){
            binding.apply {
                val item=getItem(absoluteAdapterPosition)
                imageView.loadImage(item?.imageUrl)
                tvTitle.text=item?.title
                val desc=item?.summary?:""
                tvDesc.setResizableText(desc, 3, true){
                    item?.let { news -> onclick(news) }
                }
                cardView.setOnClickListener {
                    item?.let { news -> onclick(news) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemNewsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData()
    }
}


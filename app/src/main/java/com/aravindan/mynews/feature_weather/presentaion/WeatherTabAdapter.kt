package com.aravindan.mynews.feature_weather.presentaion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.aravindan.mynews.databinding.ItemTabWeatherBinding
import com.aravindan.mynews.feature_weather.domain.model.WeatherTab

class WeatherTabAdapter (val tabs:List<WeatherTab>,private val onClick:(WeatherTab)->Unit):RecyclerView.Adapter<WeatherTabAdapter.ViewHolder>(){
    inner class ViewHolder(val binding:ItemTabWeatherBinding):RecyclerView.ViewHolder(binding.root){
        fun bindData(){
            val item=tabs.get(absoluteAdapterPosition)
            binding.tvTitle.text=item.title
            binding.view.isVisible=item.isSelected
            itemView.setOnClickListener {
                tabs.forEachIndexed { index, weatherTab ->
                    weatherTab.isSelected = absoluteAdapterPosition==index
                }
                notifyDataSetChanged()
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(ItemTabWeatherBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int =tabs.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData()
    }

}
package com.aravindan.mynews.feature_weather.presentaion

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aravindan.mynews.core.util.Constants
import com.aravindan.mynews.core.util.MyUtil
import com.aravindan.mynews.core.util.loadImage
import com.aravindan.mynews.databinding.ItemForecastBinding
import com.aravindan.mynews.feature_weather.domain.model.ForecastDay


class ForecastAdapter(private val onClick:(ForecastDay?)->Unit):ListAdapter<ForecastDay?,ForecastAdapter.ViewHolder>(DIFF_UTIL) {
    companion object{
        private val DIFF_UTIL=object :DiffUtil.ItemCallback<ForecastDay?>(){
            override fun areItemsTheSame(oldItem: ForecastDay, newItem: ForecastDay): Boolean =false
            override fun areContentsTheSame(oldItem: ForecastDay, newItem: ForecastDay): Boolean {
                return false
            }
        }
    }

    inner class ViewHolder(val binding: ItemForecastBinding):RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun binData(){
            binding.apply {
                val item=getItem(absoluteAdapterPosition)
                tvDay.text=MyUtil.getDateFromString(item?.date,Constants.DAY_FORMAT,Constants.API_DATE_FORMAT2)
                tvTemp.text="${item?.day?.avgtempC}℃"
                imageWeather.loadImage("https:"+item?.day?.condition?.icon)
                if (item?.isSelected == true){
                    cardView.strokeWidth=4
                }else{
                    cardView.strokeWidth=0
                }
                cardView.setOnClickListener {
                    onClick(item)
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemForecastBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binData()
    }
}
package com.aravindan.mynews.feature_weather.domain

import com.aravindan.mynews.R

class WeatherUtlil {
    companion object{

        fun getAirQuality(index:Int?):Pair<Int,Int>{
            return when(index){
                1->Pair(R.string.good, R.color.green)
                2->Pair(R.string.moderate, R.color.yellow)
                3->Pair(R.string.un_healthy_for_sensitive, R.color.orange)
                4->Pair(R.string.un_healthy, R.color.red)
                5->Pair(R.string.very_un_healthy, R.color.purple)
                6->Pair(R.string.hazardous, R.color.brown)
                else->Pair(0,0)
            }
        }
    }

}
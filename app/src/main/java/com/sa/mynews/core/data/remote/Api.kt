package com.sa.mynews.core.data.remote

import com.sa.mynews.feature_news.domain.model.NewsRes
import com.sa.mynews.feature_weather.domain.model.WeatherRes
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("articles")
    suspend fun getNews(
    @Query("format") format:String="json",
    @Query("limit") limit:Int=20,
    @Query("offset") offset:Int):Result<NewsRes>


    @GET("forecast.json")
    suspend fun getWeather(
        @Query("q") query:String,
        @Query("aqi")aqi:String="yes",
        @Query("days")days:String="7"
    ):Result<WeatherRes>
}
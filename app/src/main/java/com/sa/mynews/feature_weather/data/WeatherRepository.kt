package com.sa.mynews.feature_weather.data

import android.app.Application
import com.sa.mynews.core.data.remote.Api
import com.sa.mynews.core.data.remote.Resource
import com.sa.mynews.feature_weather.domain.model.WeatherRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository(val api:Api,app:Application) {
    suspend fun getWeather(latitude:Double?,longitude:Double?): Flow<Resource<WeatherRes?>> {
        return flow {
            emit(Resource.Loading())
            api.getWeather(query ="$latitude,$longitude" ).onSuccess {
                emit(Resource.Success(it))
            }.onFailure {
                emit(Resource.Error(it.localizedMessage))
            }
        }
    }
}
package com.aravindan.mynews.feature_weather.presentaion

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aravindan.mynews.core.data.location.DefaultLocationTracker
import com.aravindan.mynews.core.data.location.LocationResource
import com.aravindan.mynews.core.data.remote.Resource
import com.aravindan.mynews.feature_weather.data.WeatherRepository
import com.aravindan.mynews.feature_weather.domain.model.ForecastDay
import com.aravindan.mynews.feature_weather.domain.model.GraphType
import com.aravindan.mynews.feature_weather.domain.model.WeatherRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationTracker: DefaultLocationTracker,
    private val repository: WeatherRepository
): ViewModel() {
    val locations= MutableStateFlow<LocationResource>(LocationResource.Loading())
    val weatherInfoFlow= MutableStateFlow<Resource<WeatherRes?>>(Resource.Success(null))
    val graphType= MutableStateFlow<GraphType>(GraphType.TEMPERATURE)
    val forecastDay=MutableStateFlow<ForecastDay?>(null)
    init {
        intFun()
    }
    private fun intFun() {
        viewModelScope.launch {
            locations.collect{
                if (it is LocationResource.Success){
                    getWeather(it.location)
                }
            }
        }
        getLocation()
    }

    fun getLocation(){
        viewModelScope.launch {
            locations.value=LocationResource.Loading()
            locations.value=locationTracker.getLocation()
        }
    }
    private fun getWeather(location: Location?){
        viewModelScope.launch {
            repository.getWeather(location?.latitude,location?.longitude).collect{
                if (it is Resource.Success){
                    if (!it.data?.forecast?.forecastday.isNullOrEmpty()){
                        if (forecastDay.value==null){
                            forecastDay.value= it.data?.forecast?.forecastday?.get(0)
                            it.data?.forecast?.forecastday?.get(0)?.isSelected=true
                        }else{
                            it.data?.forecast?.forecastday?.forEach{
                                it.isSelected = it.date==forecastDay.value?.date
                            }
                        }
                    }
                }
                weatherInfoFlow.value=it
            }
        }
    }
}
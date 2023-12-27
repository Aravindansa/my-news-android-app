package com.sa.mynews.core.data.remote

import com.sa.mynews.core.util.Constants
import okhttp3.Interceptor
import okhttp3.Response

class WeatherInterceptor():Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder=chain.request().newBuilder()
        requestBuilder.addHeader("X-RapidAPI-Key",Constants.RAPID_API_KEY)
        requestBuilder.addHeader("X-RapidAPI-Host",Constants.RAPID_API_HOST)
        return chain.proceed(requestBuilder.build())
    }
}
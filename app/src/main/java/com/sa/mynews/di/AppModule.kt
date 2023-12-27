package com.sa.mynews.di

import android.app.Application
import android.content.Context
import com.sa.mynews.core.data.local.AppDatabase
import com.sa.mynews.core.data.location.DefaultLocationTracker
import com.sa.mynews.core.data.remote.Api
import com.sa.mynews.core.data.remote.ResultCallAdapterFactory
import com.sa.mynews.core.data.remote.WeatherInterceptor
import com.sa.mynews.core.util.Constants
import com.sa.mynews.feature_news.data.NewsRepository
import com.sa.mynews.feature_weather.data.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideApi (): Api {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client= OkHttpClient.Builder().apply {
            addInterceptor(logging)
        }.build()
        val retrofit= Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
        return retrofit.create(Api::class.java)
    }
    @Singleton
    @Provides
    @Named("weather_api")
    fun provideWeatherApi (): Api {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client= OkHttpClient.Builder().apply {
            addInterceptor(logging)
            addInterceptor(WeatherInterceptor())
        }.build()
        val retrofit= Retrofit.Builder()
            .baseUrl(Constants.WEATHER_API)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
        return retrofit.create(Api::class.java)
    }


    @Provides
    fun provideAppContext(@ApplicationContext context: Context) = context

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) =
        AppDatabase.getDatabase(context)

    @Provides
    fun provideAppDao(appDatabase: AppDatabase) = appDatabase.appDao()

    @Singleton
    @Provides
    fun provideNewsRepository(api: Api,app: Application,db:AppDatabase):NewsRepository{
        return NewsRepository(app,api,db)
    }
    @Singleton
    @Provides
    fun provideWeatherRepository(@Named("weather_api") api: Api,app: Application): WeatherRepository {
        return WeatherRepository(api,app)
    }
    @Provides
    @Singleton
    fun provideFusedLocationClient(app: Application):FusedLocationProviderClient{
        return LocationServices.getFusedLocationProviderClient(app)
    }
    @Provides
    @Singleton
    fun provideLocationTracker(locationProviderClient: FusedLocationProviderClient,app: Application):DefaultLocationTracker{
        return DefaultLocationTracker(locationProviderClient,app)
    }


}
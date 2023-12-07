package com.aravindan.mynews.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MyUtil {
    companion object{

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
            return false

        }
        fun getCalender(dateString:String?,format:String=Constants.API_DATE_FORMAT):Calendar?{
            if(dateString==null) return null
            val inputFormat = SimpleDateFormat(format,Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date!=null){
                val calender= Calendar.getInstance()
                calender.time=date
                return calender
            }else{
                return null
            }
        }
        fun getTodayDate(toPattern:String=Constants.API_DATE_FORMAT2):String{
            val simpleDateFormat= SimpleDateFormat(toPattern, Locale.getDefault());
            try {
                val date = Calendar.getInstance().time
                return simpleDateFormat.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }

        }

        fun getDateFromString(dateString:String?,toPattern:String=Constants.TIME_FORMAT,fromFormat:String=Constants.API_DATE_FORMAT):String{
            val inputFormat = SimpleDateFormat(fromFormat,Locale.getDefault())
            val simpleDateFormat= SimpleDateFormat(toPattern, Locale.getDefault());
            try {
                if (dateString == null) return ""
                val date = inputFormat.parse(dateString) ?: return ""
                return simpleDateFormat.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }

        }

    }
}
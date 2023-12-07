package com.aravindan.mynews.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PermissionUtlil {
   private const val DENIED="DENIED"
   private const val EXPLAINED="EXPLAINED"
    fun hasLocationPermission(context:Context):Boolean{
        val hasAccessFineLocation=ContextCompat.checkSelfPermission(
            context,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        val hasAccessCrossLocation=ContextCompat.checkSelfPermission(
            context,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
        if (!hasAccessFineLocation ||!hasAccessCrossLocation)
            return false
        return true
    }
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun shouldShowRational(activity: FragmentActivity,result:Map<String,  Boolean>):Boolean{
        val deniedList: List<String> = result.filter {
            !it.value
        }.map {
            it.key
        }

        when {
            deniedList.isNotEmpty() -> {
                val map = deniedList.groupBy { permission ->
                    if (shouldShowRequestPermissionRationale(activity,permission)) DENIED else EXPLAINED
                }
                map[DENIED]?.let {
                    return false
                }
                map[EXPLAINED]?.let {
                    return true
                }
            }
        }
        return false
    }

}
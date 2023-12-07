package com.aravindan.mynews.core.data.location

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.aravindan.mynews.R
import com.aravindan.mynews.core.util.MyUtil
import com.aravindan.mynews.core.util.PermissionUtlil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class DefaultLocationTracker (
    private val locationProviderClient: FusedLocationProviderClient,
    private val application: Application
):LocationTracker {
    @SuppressLint("MissingPermission")
    override suspend fun getLocation(): LocationResource {
        val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!PermissionUtlil.hasLocationPermission(application)){
            return LocationResource.NoPermission()
        } else if(!MyUtil.isOnline(application)){
            return LocationResource.Error(application.getString(R.string.internet_connection_alert))
        }
        else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return LocationResource.LocationNotEnabled()
        }
        val location: Location? = suspendCoroutine {cont->
            locationProviderClient.lastLocation.apply {
                if (isComplete && isSuccessful && result!=null){
                    cont.resume(result)
                }else{
                    cont.resume(null)
                }
                return@suspendCoroutine
            }
        }
        if (location!=null){
            return LocationResource.Success(location)
        }else{
            val newLocation =getNewLocation()
            if (newLocation!=null){
                return LocationResource.Success(newLocation)
            }else{
                return LocationResource.Error(application.getString(R.string.something_went_wrong))
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getNewLocation():Location?= suspendCoroutine {
        val request=LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000
        ).apply {
            setMinUpdateDistanceMeters(0f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(false)
        }.build()
        val callback=object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                try {
                    locationProviderClient.flushLocations()
                    locationProviderClient.removeLocationUpdates(this)
                    if (p0.locations.isNotEmpty()){
                        it.resumeWith(Result.success(p0.locations[0]))
                    }else{
                        it.resumeWith(Result.success(p0.lastLocation))
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                    it.resumeWith(Result.success(null))
                }
            }
        }
        locationProviderClient.requestLocationUpdates(request,callback,Looper.getMainLooper())
    }



}
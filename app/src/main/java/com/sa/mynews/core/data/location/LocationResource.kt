package com.sa.mynews.core.data.location

import android.location.Location

sealed class LocationResource(val location: Location?=null,val error:String?=null) {
    class Loading():LocationResource()
    class LocationNotEnabled(): LocationResource()
    class NoPermission(): LocationResource()
    class Success (location: Location?): LocationResource(location=location)
    class Error(error: String): LocationResource(error = error)
}
package com.aravindan.mynews.core.data.location

interface LocationTracker {
    suspend fun getLocation():LocationResource
}
package com.sa.mynews.core.data.location

interface LocationTracker {
    suspend fun getLocation():LocationResource
}
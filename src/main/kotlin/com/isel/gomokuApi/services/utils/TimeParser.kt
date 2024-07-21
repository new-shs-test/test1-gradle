package com.isel.gomokuApi.services.utils

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TimeParser {
    fun convertToTime(timeInMillis : Long) : Duration = timeInMillis.toDuration(DurationUnit.SECONDS)
}
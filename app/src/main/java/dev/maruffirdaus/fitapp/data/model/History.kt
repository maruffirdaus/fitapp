package dev.maruffirdaus.fitapp.data.model

import kotlin.time.Duration

data class History(
    val date: String,
    val startingPoint: String,
    val endPoint: String,
    val inputDistance: Int,
    val outputDistance: Int,
    val algorithm: Int,
    val runningTime: Duration
)

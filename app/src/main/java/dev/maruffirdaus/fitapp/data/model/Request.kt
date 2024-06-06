package dev.maruffirdaus.fitapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Request(
    val startingPoint: Int,
    val distance: Int,
    val algorithm: Int
) : Parcelable

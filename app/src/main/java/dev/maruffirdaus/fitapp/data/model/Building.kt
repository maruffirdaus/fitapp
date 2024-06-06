package dev.maruffirdaus.fitapp.data.model

data class Building(
    val id: Int,
    val name: String,
    val adjacent: List<AdjacentBuilding>
)

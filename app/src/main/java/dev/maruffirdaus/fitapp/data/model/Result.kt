package dev.maruffirdaus.fitapp.data.model

data class Result(
    var distance: Int = 0,
    var points: List<Int> = listOf(),
    var paths: List<Int> = listOf()
)
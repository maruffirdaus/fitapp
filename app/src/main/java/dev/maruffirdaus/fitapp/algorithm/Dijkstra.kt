package dev.maruffirdaus.fitapp.algorithm

import dev.maruffirdaus.fitapp.data.model.Building
import dev.maruffirdaus.fitapp.data.model.Result
import kotlin.math.absoluteValue

fun dijkstra(buildings: List<Building>, startingPoint: Int, targetDistance: Int): Result? {
    val distances = Array(buildings.size) { Int.MAX_VALUE }
    val isVisited = Array(buildings.size) { false }
    val points = Array(buildings.size) { intArrayOf(startingPoint) }
    val paths = Array(buildings.size) { intArrayOf() }

    distances[startingPoint] = 0

    var isAllVisited = false

    while (!isAllVisited) {
        var cursor = -1

        for (i in 0..buildings.lastIndex) {
            if (!isVisited[i] && (cursor == -1 || distances[i] < distances[cursor])) {
                cursor = i
            }
        }

        if (cursor == -1) {
            isAllVisited = true
        } else {
            isVisited[cursor] = true

            for (point in buildings[cursor].adjacent) {
                if (!isVisited[point.id] && distances[cursor] + point.distance < distances[point.id]) {
                    distances[point.id] = distances[cursor] + point.distance
                    points[point.id] = points[cursor] + intArrayOf(point.id)
                    paths[point.id] = paths[cursor] + intArrayOf(point.pathId)
                }
            }
        }
    }

    var distance = Int.MAX_VALUE
    var result: Int? = null

    for (i in distances.indices) {
        val diffCurrent = (targetDistance - distances[i]).absoluteValue
        val diffResult = (targetDistance - distance).absoluteValue

        if (distances[i] in targetDistance - 50..targetDistance + 50 && diffCurrent < diffResult) {
            distance = distances[i]
            result = i
        }
    }

    return if (result != null) {
        Result(distance, points[result].toList(), paths[result].toList())
    } else {
        null
    }
}
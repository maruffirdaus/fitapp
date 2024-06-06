package dev.maruffirdaus.fitapp.algorithm

import dev.maruffirdaus.fitapp.data.model.Building
import dev.maruffirdaus.fitapp.data.model.Result

fun backtracking(
    buildings: List<Building>,
    currentPoint: Int,
    targetDistance: Int,
    isSelected: Array<Boolean> = Array(buildings.size) { false },
    currentDistance: Int = 0,
    points: IntArray = intArrayOf(currentPoint),
    paths: IntArray = intArrayOf()
): Result? {
    if (!isSelected[currentPoint] && currentDistance <= targetDistance + 50) {
        if (targetDistance - 50 <= currentDistance) {
            return Result(currentDistance, points.toList(), paths.toList())
        } else {
            var result: Result? = null
            isSelected[currentPoint] = true

            var i = 0

            while (i < buildings[currentPoint].adjacent.size && result == null) {
                val tempResult = backtracking(
                    buildings,
                    buildings[currentPoint].adjacent[i].id,
                    targetDistance,
                    isSelected.copyOf(),
                    currentDistance + buildings[currentPoint].adjacent[i].distance,
                    points + buildings[currentPoint].adjacent[i].id,
                    paths + buildings[currentPoint].adjacent[i].pathId
                )

                result = tempResult
                i++
            }

            return result
        }
    } else {
        return null
    }
}
package dev.maruffirdaus.fitapp.ui.main

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.maruffirdaus.fitapp.data.model.Building
import dev.maruffirdaus.fitapp.data.model.History
import dev.maruffirdaus.fitapp.data.model.Result

class MainViewModel : ViewModel() {
    var isInitialized = false

    private lateinit var buildings: List<Building>
    private lateinit var paths: List<Drawable?>

    private val _routeResult = MutableLiveData<Result?>()
    val routeResult: LiveData<Result?> = _routeResult

    private val _routeBitmapResult = MutableLiveData<Bitmap?>()
    val routeBitmapResult: LiveData<Bitmap?> = _routeBitmapResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isHeaderVisible = MutableLiveData<Boolean>()
    val isHeaderVisible: LiveData<Boolean> = _isHeaderVisible

    private var selectedPoint = 0

    private var history = listOf<History>()

    init {
        _routeResult.value = null
        _routeBitmapResult.value = null
    }

    fun initData(buildings: List<Building>, paths: List<Drawable?>) {
        this.buildings = buildings
        this.paths = paths
        isInitialized = true
    }

    fun getBuildingsData(): List<Building> {
        return if (isInitialized) {
            /*for (i in buildings.indices) {
                buildings[i].isSelected = false
            }*/
            buildings
        } else {
            listOf()
        }
    }

    fun getPathsData(): List<Drawable?> {
        return if (isInitialized) {
            paths
        } else {
            listOf()
        }
    }

    fun setRouteResult(result: Result?) {
        _routeResult.value = result
    }

    fun getRouteResult(): Result? {
        return _routeResult.value
    }

    fun setRouteBitmapResult(result: Bitmap?) {
        _routeBitmapResult.value = result
    }

    fun resetRouteResult() {
        _routeResult.value = null
        _routeBitmapResult.value = null
    }

    fun setLoadingStatus(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun getLoadingStatus(): Boolean {
        return _isLoading.value ?: false
    }

    fun setHeaderVisibility(isVisible: Boolean) {
        _isHeaderVisible.value = isVisible
    }

    fun setSelectedPoint(point: Int) {
        selectedPoint = point
    }

    fun getSelectedPoint(): Int {
        return selectedPoint
    }

    fun addNewHistory(history: History) {
        if (this.history.size < 6) {
            this.history = listOf(history) + this.history
        } else {
            this.history = listOf(history)
        }
    }

    fun getHistory(): List<History> = history

    fun clearHistory() {
        history = listOf()
    }
}
package dev.maruffirdaus.fitapp.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.maruffirdaus.fitapp.R
import dev.maruffirdaus.fitapp.algorithm.backtracking
import dev.maruffirdaus.fitapp.algorithm.dijkstra
import dev.maruffirdaus.fitapp.data.model.AdjacentBuilding
import dev.maruffirdaus.fitapp.data.model.Building
import dev.maruffirdaus.fitapp.data.model.History
import dev.maruffirdaus.fitapp.data.model.Request
import dev.maruffirdaus.fitapp.data.model.Result
import dev.maruffirdaus.fitapp.databinding.DetailDialogBinding
import dev.maruffirdaus.fitapp.databinding.FragmentRouteBinding
import dev.maruffirdaus.fitapp.ui.findroute.FindRouteActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.measureTimedValue

class RouteFragment : Fragment() {
    private lateinit var binding: FragmentRouteBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private var topInset = 0
    private var maxMapWidth by Delegates.notNull<Int>()
    private var minMapWidth by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteBinding.inflate(layoutInflater)
        maxMapWidth = requireActivity().resources.getDimensionPixelSize(R.dimen.max_map_width)
        minMapWidth = requireActivity().resources.getDimensionPixelSize(R.dimen.min_map_width)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInset()
        initData()
        observeData()
        setMapView()
        setButton()
        configureLoadingScreen()
    }

    private fun setInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.header) { view, windowInsets ->
            topInset = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val layoutParams = view.layoutParams
            layoutParams.height =
                requireActivity().resources.getDimensionPixelSize(R.dimen.header_height) + topInset
            view.layoutParams = layoutParams
            view.updatePadding(top = topInset)

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initData() {
        if (!viewModel.isInitialized) {
            var buildings = arrayOf<Building>()
            val buildingNames = resources.getStringArray(R.array.building_names)
            val adjacentBuildings = resources.getStringArray(R.array.adjacent_buildings)
            val adjacentPaths = resources.getStringArray(R.array.adjacent_paths)
            val pathDistances = resources.getIntArray(R.array.path_distances)

            buildingNames.forEachIndexed { index, name ->
                var adjacent = arrayOf<AdjacentBuilding>()
                var i = 0
                while (i < adjacentBuildings[index].length) {
                    adjacent += AdjacentBuilding(
                        adjacentBuildings[index].slice(i..i + 1).toInt(),
                        adjacentPaths[index].slice(i..i + 1).toInt(),
                        pathDistances[adjacentPaths[index].slice(i..i + 1).toInt()]
                    )
                    i += 2
                }
                buildings += Building(index, name, adjacent.toList())
            }

            val pathDrawables = resources.obtainTypedArray(R.array.paths)
            var paths = arrayOf<Drawable?>()

            for (i in 0..16) {
                paths += pathDrawables.getDrawable(i)
            }

            pathDrawables.recycle()

            viewModel.initData(buildings.toList(), paths.toList())
        }
    }

    private fun observeData() {
        viewModel.routeResult.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.distance.text = buildString {
                    append(it.distance.toString())
                    append(" m")
                }
            }
        }
        viewModel.routeBitmapResult.observe(viewLifecycleOwner) {
            if (it != null) {
                with(binding) {
                    route.setImageBitmap(it)
                    route.visibility = View.VISIBLE
                }
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            with(binding) {
                if (it) {
                    loadingScreen.visibility = View.VISIBLE
                    toggleButton()
                    toggleResetButton()
                } else {
                    loadingScreen.visibility = View.GONE
                    toggleButton()
                    toggleResetButton()
                }
            }
        }
        viewModel.isHeaderVisible.observe(viewLifecycleOwner) {
            with(binding) {
                if (it) {
                    header.visibility = View.VISIBLE
                    setMapTopPadding(requireActivity().resources.getDimensionPixelSize(R.dimen.header_margin) + topInset)
                } else {
                    header.visibility = View.GONE
                    setMapTopPadding(0)
                }
            }
        }
    }

    private fun setMapTopPadding(v: Int) {
        with(binding) {
            horizontalScrollView.updatePadding(top = v)

            val scrollY = scrollView.scrollY + v

            scrollView.post {
                scrollView.scrollTo(scrollView.scrollX, scrollY)
            }
        }
    }

    private fun setMapView() {
        with(binding) {
            horizontalScrollView.post {
                val scrollX =
                    (requireActivity().resources.getDimensionPixelSize(R.dimen.max_map_width) / 2 - horizontalScrollView.width) / 2
                if (0 < scrollX) horizontalScrollView.scrollTo(scrollX, 0)
            }
            scrollView.post {
                val scrollY =
                    (requireActivity().resources.getDimensionPixelSize(R.dimen.max_map_height) / 2 - scrollView.height) / 2
                if (0 < scrollY) scrollView.scrollTo(0, scrollY)
            }
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == FindRouteActivity.SUCCESS) {
            val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(
                    FindRouteActivity.EXTRA_REQUEST,
                    Request::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                (result.data?.getParcelableExtra(FindRouteActivity.EXTRA_REQUEST))
            }

            if (request != null) {
                lifecycleScope.launch {
                    viewModel.setLoadingStatus(true)

                    val buildings = viewModel.getBuildingsData()
                    var routeResult: Result?
                    var timeTaken: Duration

                    withContext(Dispatchers.Default) {
                        if (request.algorithm == 0) {
                            val (tempRouteResult, tempTimeTaken) = measureTimedValue {
                                backtracking(
                                    buildings,
                                    request.startingPoint,
                                    request.distance
                                )
                            }
                            routeResult = tempRouteResult
                            timeTaken = tempTimeTaken
                        } else {
                            val (tempRouteResult, tempTimeTaken) = measureTimedValue {
                                dijkstra(
                                    buildings,
                                    request.startingPoint,
                                    request.distance
                                )
                            }
                            routeResult = tempRouteResult
                            timeTaken = tempTimeTaken
                        }
                    }

                    if (routeResult != null) {
                        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                        val currentDateTime = LocalDateTime.now().format(formatter)

                        viewModel.addNewHistory(
                            History(
                                currentDateTime,
                                buildings[routeResult!!.points[0]].name,
                                buildings[routeResult!!.points[routeResult!!.points.lastIndex]].name,
                                request.distance,
                                routeResult!!.distance,
                                request.algorithm,
                                timeTaken
                            )
                        )

                        viewModel.setSelectedPoint(0)
                        viewModel.setRouteResult(routeResult)
                        viewModel.setHeaderVisibility(true)
                        loadRoute(routeResult!!.paths)
                        binding.scrollView.post {
                            viewModel.setLoadingStatus(false)
                        }
                        showRunningTime(timeTaken)
                    } else {
                        viewModel.setHeaderVisibility(false)
                        binding.route.visibility = View.GONE
                        viewModel.resetRouteResult()
                        binding.scrollView.post {
                            viewModel.setLoadingStatus(false)
                        }
                        showFailDialog()
                    }
                }
            }
        }
    }

    private suspend fun loadRoute(routeResult: List<Int>) {
        val paths = viewModel.getPathsData()
        val routeBitmap = Bitmap.createBitmap(
            resources.getDimensionPixelSize(R.dimen.min_map_width),
            resources.getDimensionPixelSize(R.dimen.min_map_height),
            Bitmap.Config.ARGB_8888
        )
        val routeCanvas = Canvas(routeBitmap)

        withContext(Dispatchers.Default) {
            for (i in routeResult) {
                paths[i]?.let { routeCanvas.drawBitmap(it.toBitmap(), 0F, 0F, null) }
                binding.route.invalidate()
            }
        }

        viewModel.setRouteBitmapResult(routeBitmap)
    }

    private fun showRunningTime(timeTaken: Duration) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Running time")
            .setMessage(timeTaken.toString())
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showFailDialog() {
        MaterialAlertDialogBuilder(
            requireActivity(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setIcon(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_cancel,
                    context?.theme
                )
            )
            .setTitle("Rute tidak ditemukan")
            .setMessage("Rute tidak ditemukan. Ubah parameter untuk menemukan rute yang lain.")
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun toggleButton() {
        with(binding) {
            findRouteButton.isEnabled = loadingScreen.visibility == View.GONE
            resetRouteButton.isEnabled = loadingScreen.visibility == View.GONE

            if (loadingScreen.visibility == View.GONE) {
                zoomInButton.isEnabled = mapFrame.layoutParams.width < maxMapWidth - 4
                zoomOutButton.isEnabled = minMapWidth < mapFrame.layoutParams.width
            } else {
                zoomInButton.isEnabled = loadingScreen.visibility == View.GONE
                zoomOutButton.isEnabled = loadingScreen.visibility == View.GONE
            }
        }
    }

    private fun toggleResetButton() {
        with(binding) {
            resetRouteButton.isEnabled =
                viewModel.getRouteResult() != null && viewModel.getLoadingStatus() == false
        }
    }

    private fun setButton() {
        with(binding) {
            findRouteButton.setOnClickListener {
                resultLauncher.launch(
                    Intent(requireActivity(), FindRouteActivity::class.java)
                )
            }
            resetRouteButton.setOnClickListener {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Reset rute?")
                    .setMessage("Rute akan direset.")
                    .setPositiveButton("Ya") { _, _ ->
                        viewModel.setLoadingStatus(true)
                        route.visibility = View.GONE
                        viewModel.resetRouteResult()
                        viewModel.setHeaderVisibility(false)

                        val scrollY =
                            scrollView.scrollY - requireActivity().resources.getDimensionPixelSize(R.dimen.header_margin) - topInset

                        scrollView.post {
                            lifecycleScope.launch {
                                scrollView.scrollTo(scrollView.scrollX, scrollY)
                                delay(250L)
                                viewModel.setLoadingStatus(false)
                            }
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            zoomInButton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

                val layoutParams = mapFrame.layoutParams

                layoutParams.width *= 2
                layoutParams.height *= 2
                mapFrame.layoutParams = layoutParams
                horizontalScrollView.scrollTo(horizontalScrollView.scrollX * 2, 0)
                scrollView.scrollTo(0, scrollView.scrollY * 2)
                zoomInButton.isEnabled = layoutParams.width < maxMapWidth - 4
                zoomOutButton.isEnabled = minMapWidth < layoutParams.width
            }
            zoomOutButton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

                val layoutParams = mapFrame.layoutParams

                layoutParams.width /= 2
                layoutParams.height /= 2
                mapFrame.layoutParams = layoutParams
                horizontalScrollView.scrollTo(horizontalScrollView.scrollX / 2, 0)
                scrollView.scrollTo(0, scrollView.scrollY / 2)
                zoomInButton.isEnabled = layoutParams.width < maxMapWidth - 4
                zoomOutButton.isEnabled = minMapWidth < layoutParams.width
            }
            detailButton.setOnClickListener {
                showDetail()
            }
        }
    }

    private fun showDetail() {
        val detailDialogBinding = DetailDialogBinding.inflate(layoutInflater)
        val buildings = viewModel.getBuildingsData()
        val distances = resources.getIntArray(R.array.path_distances)
        val result = viewModel.getRouteResult()
        var selected = viewModel.getSelectedPoint()

        if (result != null) {
            with(detailDialogBinding) {
                fun applyStartPoint() {
                    prevSection.visibility = View.GONE
                }

                fun applyFinishPoint() {
                    nextSection.visibility = View.GONE
                }

                fun modifyPrevPoint(buildingIndex: Int, pathIndex: Int) {
                    prevSection.visibility = View.VISIBLE
                    prevPoint.text = buildings[result.points[buildingIndex]].name
                    prevDistance.text = buildString {
                        append(distances[result.paths[pathIndex]].toString())
                        append(" m")
                    }
                }

                fun modifyNextPoint(buildingIndex: Int, pathIndex: Int) {
                    nextSection.visibility = View.VISIBLE
                    nextPoint.text = buildings[result.points[buildingIndex]].name
                    nextDistance.text = buildString {
                        append(distances[result.paths[pathIndex]].toString())
                        append(" m")
                    }
                }

                buildingName.text = buildings[result.points[selected]].name

                when (selected) {
                    0 -> {
                        applyStartPoint()
                        modifyNextPoint(selected + 1, selected)
                    }

                    result.points.size - 1 -> {
                        applyFinishPoint()
                        modifyPrevPoint(selected - 1, selected - 1)
                    }

                    else -> {
                        modifyPrevPoint(selected - 1, selected - 1)
                        modifyNextPoint(selected + 1, selected)
                    }
                }

                prevButton.setOnClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

                    selected--
                    viewModel.setSelectedPoint(selected)
                    buildingName.text = buildings[result.points[selected]].name

                    when (selected) {
                        0 -> {
                            applyStartPoint()
                            modifyNextPoint(selected + 1, selected)
                        }

                        result.points.size - 1 -> {
                            applyFinishPoint()
                            modifyPrevPoint(selected - 1, selected - 1)
                        }

                        else -> {
                            modifyPrevPoint(selected - 1, selected - 1)
                            modifyNextPoint(selected + 1, selected)
                        }
                    }
                }

                nextButton.setOnClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

                    selected++
                    viewModel.setSelectedPoint(selected)
                    buildingName.text = buildings[result.points[selected]].name

                    when (selected) {
                        0 -> {
                            applyStartPoint()
                            modifyNextPoint(selected + 1, selected)
                        }

                        result.points.size - 1 -> {
                            applyFinishPoint()
                            modifyPrevPoint(selected - 1, selected - 1)
                        }

                        else -> {
                            modifyPrevPoint(selected - 1, selected - 1)
                            modifyNextPoint(selected + 1, selected)
                        }
                    }
                }
            }
        }

        MaterialAlertDialogBuilder(requireActivity())
            .setView(detailDialogBinding.root)
            .show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configureLoadingScreen() {
        binding.loadingScreen.setOnTouchListener { _, _ ->
            true
        }
    }
}
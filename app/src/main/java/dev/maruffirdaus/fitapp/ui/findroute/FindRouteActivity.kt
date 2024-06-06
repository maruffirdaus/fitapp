package dev.maruffirdaus.fitapp.ui.findroute

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.maruffirdaus.fitapp.R
import dev.maruffirdaus.fitapp.data.model.Request
import dev.maruffirdaus.fitapp.databinding.ActivityFindRouteBinding

class FindRouteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindRouteBinding
    private var buildingNames = arrayOf<String>()
    private var selectedBuilding: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFindRouteBinding.inflate(layoutInflater)
        setInset()
        setContentView(binding.root)
        setContent()
        setButton()
        setTextOnChangedListener()
    }

    private fun setInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.inset) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val isKeyboardVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = insets.bottom)

            if (isKeyboardVisible) {
                binding.appBarLayout.setExpanded(false)
                v.updatePadding(bottom = imeInsets.bottom)
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setContent() {
        val typedArray = resources.obtainTypedArray(R.array.building_names)
        for (i in 0..10) {
            buildingNames += typedArray.getString(i) ?: resources.getString(R.string.building)
        }
        typedArray.recycle()
        binding.buildingName.text = buildingNames[selectedBuilding]
    }

    private fun setButton() {
        with(binding) {
            appBar.setNavigationOnClickListener {
                finish()
            }
            prevButton.isEnabled = false
            prevButton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

                selectedBuilding--
                binding.buildingName.text = buildingNames[selectedBuilding]

                when (selectedBuilding) {
                    0 -> {
                        prevButton.isEnabled = false
                        nextButton.isEnabled = true
                    }
                    buildingNames.size - 1 -> {
                        prevButton.isEnabled = true
                        nextButton.isEnabled = false
                    }
                    else -> {
                        prevButton.isEnabled = true
                        nextButton.isEnabled = true
                    }
                }
            }
            nextButton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

                selectedBuilding++
                binding.buildingName.text = buildingNames[selectedBuilding]

                when (selectedBuilding) {
                    0 -> {
                        prevButton.isEnabled = false
                        nextButton.isEnabled = true
                    }
                    buildingNames.size - 1 -> {
                        prevButton.isEnabled = true
                        nextButton.isEnabled = false
                    }
                    else -> {
                        prevButton.isEnabled = true
                        nextButton.isEnabled = true
                    }
                }
            }
            getRouteButton.setOnClickListener {
                with(binding) {
                    val distance = if (distanceEditText.text.isNullOrEmpty()) {
                        0
                    } else {
                        Integer.parseInt(distanceEditText.text.toString())
                    }
                    if (distance in 150..3000) {
                        val algorithm = if (backtracking.isChecked) {
                            0
                        } else {
                            1
                        }
                        val resultIntent = Intent()
                        resultIntent.putExtra(EXTRA_REQUEST, Request(selectedBuilding, distance, algorithm))
                        setResult(SUCCESS, resultIntent)
                        finish()
                    } else {
                        distanceTextInput.error = "Jarak harus berada pada rentang 150-3000 m"
                        MaterialAlertDialogBuilder(this@FindRouteActivity)
                            .setTitle("Jarak tidak sesuai")
                            .setMessage("Jarak harus berada pada rentang 150-2850 m.")
                            .setPositiveButton("Tutup") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }
        }
    }

    private fun setTextOnChangedListener() {
        binding.distanceEditText.doOnTextChanged { _, _, _, _ ->
            binding.distanceTextInput.error = null
        }
    }

    companion object {
        const val EXTRA_REQUEST = "extra_request"
        const val SUCCESS = 200
    }
}
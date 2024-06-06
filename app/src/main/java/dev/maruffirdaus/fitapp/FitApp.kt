package dev.maruffirdaus.fitapp

import android.app.Application
import android.graphics.Color
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class FitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setContentBasedSource(Color.parseColor("#baf396"))
                .build()
        )
    }
}

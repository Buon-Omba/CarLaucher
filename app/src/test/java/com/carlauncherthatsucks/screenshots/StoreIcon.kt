package com.carlauncherthatsucks.screenshots

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.carlauncherthatsucks.R
import org.junit.Rule
import org.junit.Test

/** Icona 512×512 per F-Droid (fastlane/metadata/android/<lingua>/images/icon.png). */
class StoreIcon {
    @get:Rule val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig(
            screenWidth = 512, screenHeight = 512, xdpi = 160, ydpi = 160, density = Density.MEDIUM,
        ),
        showSystemUi = false,
    )

    @Test fun icon() = paparazzi.snapshot {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0xFF006C4A))
        ) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize().scale(1.3f))
        }
    }
}

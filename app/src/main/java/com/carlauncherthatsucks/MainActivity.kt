package com.carlauncherthatsucks

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carlauncherthatsucks.ui.CarLauncherApp
import com.carlauncherthatsucks.ui.CarTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: LauncherViewModel by viewModels()

    /** Incrementato quando si preme "Home" mentre il launcher è già aperto. */
    private val homePresses = mutableIntStateOf(0)

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.onLocationPermissionResult() }

    @SuppressLint("SourceLockedOrientationActivity") // richiesta esplicita: solo verticale
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Rigorosamente verticale, anche se il manifest venisse ignorato.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.prefs.settings.collect { s ->
                    if (s.keepScreenOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }

        setContent {
            CarTheme {
                CarLauncherApp(
                    vm = vm,
                    homePresses = homePresses.intValue,
                    requestLocationPermission = {
                        locationPermission.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    },
                )
            }
        }

        if (!vm.location.hasPermission()) {
            locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homePresses.intValue++
    }
}

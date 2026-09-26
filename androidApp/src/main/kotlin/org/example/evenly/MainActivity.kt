package org.example.evenly

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import org.example.evenly.security.AndroidBiometricAuthenticator

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        val barStyle = SystemBarStyle.light(darkScrim = Color.TRANSPARENT, scrim = Color.TRANSPARENT)
        enableEdgeToEdge(navigationBarStyle = barStyle, statusBarStyle = barStyle)
        super.onCreate(savedInstanceState)
        val biometricAuthenticator = AndroidBiometricAuthenticator(this)
        val graph = (application as EvenlyApplication).graph
        setContent { App(biometricAuthenticator = biometricAuthenticator, graph = graph) }
    }
}

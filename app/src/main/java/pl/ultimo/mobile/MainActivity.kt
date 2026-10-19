package pl.ultimo.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.ultimo.mobile.core.audio.SoundEffectsManager
import pl.ultimo.mobile.core.haptic.HapticFeedbackManager
import pl.ultimo.mobile.feature.game.GameRoute
import pl.ultimo.mobile.feature.history.HistoryScreen
import pl.ultimo.mobile.feature.lobby.LobbyScreen
import pl.ultimo.mobile.ui.theme.BackgroundDark
import pl.ultimo.mobile.ui.theme.UltimoTheme
import pl.ultimo.mobile.ui.theme.WindowSizeClass
import pl.ultimo.mobile.ui.theme.calculateWindowSizeClass
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundManager: SoundEffectsManager

    @Inject
    lateinit var hapticManager: HapticFeedbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            UltimoTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    color = BackgroundDark
                ) {
                    UltimoAppNavHost(
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        windowSizeClass = windowSizeClass
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

@Composable
fun UltimoAppNavHost(
    soundManager: SoundEffectsManager,
    hapticManager: HapticFeedbackManager,
    windowSizeClass: WindowSizeClass = calculateWindowSizeClass()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "lobby"
    ) {
        composable("lobby") {
            LobbyScreen(
                onJoinMatch = { username, roomId ->
                    navController.navigate("game/$roomId")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                }
            )
        }
        composable("game/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: "default"
            GameRoute(
                roomId = roomId,
                soundManager = soundManager,
                hapticManager = hapticManager,
                windowWidthSizeClass = windowSizeClass.widthSizeClass,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("history") {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

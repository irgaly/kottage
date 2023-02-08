package io.github.irgaly.kottage.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.get
import io.github.irgaly.kottage.platform.contextOf
import io.github.irgaly.kottage.put
import io.github.irgaly.kottage.sample.data.repository.AnimalRemoteRepository
import io.github.irgaly.kottage.sample.ui.MenuScreen
import io.github.irgaly.kottage.sample.ui.PagingScreen
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        WindowInsetsControllerCompat(
            window,
            findViewById(android.R.id.content)
        ).isAppearanceLightStatusBars = true
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val kottage = remember(context) {
                    Kottage(
                        name = "sample",
                        directoryPath = context.cacheDir.absolutePath,
                        KottageEnvironment(contextOf(context))
                    )
                }
                val animalRemoteRepository = remember {
                    AnimalRemoteRepository()
                }
                NavHost(navController, startDestination = "menu") {
                    composable("menu") {
                        MenuScreen(
                            onNavigatePaging = {
                                navController.navigate("paging")
                            }
                        )
                    }
                    composable("paging") {
                        PagingScreen(
                            kottage = kottage,
                            animalRemoteRepository = animalRemoteRepository
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    val kottage2 = Kottage(
                        "test",
                        cacheDir.absolutePath,
                        KottageEnvironment(contextOf(baseContext))
                    )
                    val storage = kottage2.storage("test")
                    lifecycleScope.launch {
                        storage.put("test", "test")
                        val read = storage.get<String>("test")
                        Log.d("kottage", "read = $read")
                        Log.d("kottage", kottage2.getDatabaseStatus())
                    }
                }
            }
        }
    }
}

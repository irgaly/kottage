package io.github.irgaly.kottage.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.get
import io.github.irgaly.kottage.platform.contextOf
import io.github.irgaly.kottage.put
import io.github.irgaly.kottage.sample.ui.MenuScreen
import io.github.irgaly.kottage.sample.ui.PagingScreen
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "menu") {
                    composable("menu") {
                        MenuScreen(
                            onNavigatePaging = {
                                navController.navigate("paging")
                            }
                        )
                    }
                    composable("paging") {
                        PagingScreen()
                    }
                }
                BackHandler(
                    enabled = (navController.previousBackStackEntry != null)
                ) {
                    navController.popBackStack()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val kottage = Kottage(
            "test",
            cacheDir.absolutePath,
            KottageEnvironment(contextOf(baseContext))
        )

        val storage = kottage.storage("test")
        lifecycleScope.launch {
            storage.put("test", "test")
            val read = storage.get<String>("test")
            Log.d("kottage", "read = $read")
            Log.d("kottage", kottage.getDatabaseStatus())
        }
    }
}

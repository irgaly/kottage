package io.github.irgaly.kottage.sample.ui

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.get
import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.platform.contextOf
import io.github.irgaly.kottage.put
import io.github.irgaly.kottage.sample.R
import kotlinx.coroutines.launch

class MainActivity: AppCompatActivity(R.layout.activity_main) {
    override fun onStart() {
        super.onStart()
        val kottage = Kottage(
            "test",
            cacheDir.absolutePath,
            KottageEnvironment(
                contextOf(baseContext),
                object : KottageCalendar {
                    override fun nowUnixTimeMillis(): Long {
                        return System.currentTimeMillis()
                    }
                }
            )
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

package io.github.irgaly.kottage.data.sqlite

import com.squareup.sqldelight.db.SqlDriver
import io.github.irgaly.kottage.platform.Context
import kotlinx.coroutines.CoroutineDispatcher

actual class DriverFactory actual constructor(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) {
    actual suspend fun createDriver(
        fileName: String,
        directoryPath: String,
        schema: SqlDriver.Schema
    ): SqlDriver {
        TODO()
    }
}

package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.KottageDatabaseManager
import io.github.irgaly.kottage.internal.KottageStorageImpl
import io.github.irgaly.kottage.strategy.KottageFifoStrategy
import io.github.irgaly.kottage.strategy.KottageKvsStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days

/**
 * Kotlin KVS Kottage
 */
class Kottage(
    val name: String,
    val directoryPath: String,
    val environment: KottageEnvironment,
    val json: Json = Json,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    companion object {
        /**
         * Get Sqlite database file path for delete or backup a file.
         */
        fun getDatabaseFilePath(name: String, directoryPath: String): String {
            return "${directoryPath}/${name}.db"
        }
    }

    private val databaseManager by lazy {
        KottageDatabaseManager(name, directoryPath, environment, dispatcher)
    }

    fun storage(
        name: String,
        optionsBuilder: (KottageStorageOptions.Builder.() -> Unit)? = null
    ): KottageStorage {
        val options = KottageStorageOptions.Builder(
            strategy = KottageKvsStrategy(),
            defaultExpireTime = null,
            autoClean = false
        ).apply {
            optionsBuilder?.invoke(this)
        }.build()
        return KottageStorageImpl(
            name,
            options.json ?: json,
            options,
            databaseManager,
            environment.calendar,
            dispatcher
        )
    }

    fun cache(
        name: String,
        optionsBuilder: (KottageStorageOptions.Builder.() -> Unit)? = null
    ): KottageStorage {
        val options = KottageStorageOptions.Builder(
            strategy = KottageFifoStrategy(1000),
            defaultExpireTime = 30.days,
            autoClean = true
        ).apply {
            optionsBuilder?.invoke(this)
        }.build()
        return KottageStorageImpl(
            name,
            options.json ?: json,
            options,
            databaseManager,
            environment.calendar,
            dispatcher
        )
    }

    suspend fun compact() {
        databaseManager.compact()
    }

    suspend fun clear() {
        databaseManager.deleteAll()
    }

    suspend fun getDatabaseStatus(): String {
        return databaseManager.getDatabaseStatus()
    }

    suspend fun export(file: String, directoryPath: String) {
        databaseManager.backupTo(file, directoryPath)
    }
}

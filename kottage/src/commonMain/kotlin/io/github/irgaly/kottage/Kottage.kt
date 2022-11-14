package io.github.irgaly.kottage

import io.github.irgaly.kottage.internal.KottageDatabaseManager
import io.github.irgaly.kottage.internal.KottageStorageImpl
import io.github.irgaly.kottage.internal.platform.PlatformFactory
import io.github.irgaly.kottage.platform.Files
import io.github.irgaly.kottage.platform.KottageSystemCalendar
import io.github.irgaly.kottage.strategy.KottageFifoStrategy
import io.github.irgaly.kottage.strategy.KottageKvsStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days

/**
 * Kotlin KVS Kottage
 *
 * @throws IllegalArgumentException name contains file separator
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class Kottage(
    val name: String,
    val directoryPath: String,
    val environment: KottageEnvironment,
    val json: Json = Json,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(dispatcher + SupervisorJob()),
    val optionsBuilder: (KottageOptions.Builder.() -> Unit)? = null
) {
    companion object {
        /**
         * Get Sqlite database file path for delete or backup a file.
         */
        fun getDatabaseFiles(name: String, directoryPath: String): DatabaseFiles {
            val databaseFile = "${directoryPath}/${name}.db"
            return DatabaseFiles(
                databaseFile = databaseFile,
                walFile = "$databaseFile-wal",
                shmFile = "$databaseFile-shm"
            )
        }

        /**
         * for Debug,
         * create an old version Database
         */
        suspend fun createOldDatabase(
            name: String,
            directoryPath: String,
            environment: KottageEnvironment,
            version: Int,
            dispatcher: CoroutineDispatcher = Dispatchers.Default
        ) {
            PlatformFactory().createDatabaseConnectionFactory().createOldDatabase(
                fileName = name,
                directoryPath = directoryPath,
                environment = environment,
                dispatcher = dispatcher,
                version = version
            )
        }
    }

    val options: KottageOptions = KottageOptions.Builder(
        autoCompactionDuration = 14.days,
        garbageCollectionTimeOfInvalidatedListEntries = 10.days
    ).apply {
        optionsBuilder?.invoke(this)
    }.build()

    private val databaseManager: KottageDatabaseManager =
        KottageDatabaseManager(name, directoryPath, options, environment, dispatcher, scope)

    /**
     * Simple KottageEvent Flow
     * This is a simple hot flow.
     *
     * see also [eventFlow]
     */
    val simpleEventFlow: Flow<KottageEvent> =
        databaseManager.eventFlow.map { KottageEvent.from(it) }

    init {
        require(!name.contains(Files.separator)) { "name contains separator: $name" }
    }

    /**
     * get KottageStorage for storage mode
     *
     * @param name A storage name. This must be unique in **this kottage database**.
     */
    fun storage(
        name: String,
        optionsBuilder: (KottageStorageOptions.Builder.() -> Unit)? = null
    ): KottageStorage {
        val options = KottageStorageOptions.Builder(
            strategy = KottageKvsStrategy(),
            defaultExpireTime = null,
            maxEventEntryCount = 1000,
            eventExpireTime = 30.days
        ).apply {
            optionsBuilder?.invoke(this)
        }.build()
        return KottageStorageImpl(
            name,
            options.json ?: json,
            options,
            this.options,
            databaseManager,
            environment.calendar ?: KottageSystemCalendar(),
            { databaseManager.compact() },
            dispatcher
        )
    }

    /**
     * get KottageStorage for cache mode
     *
     * @param name A storage name. This must be unique in this kottage database.
     */
    fun cache(
        name: String,
        optionsBuilder: (KottageStorageOptions.Builder.() -> Unit)? = null
    ): KottageStorage {
        val options = KottageStorageOptions.Builder(
            strategy = KottageFifoStrategy(1000),
            defaultExpireTime = 30.days,
            maxEventEntryCount = 1000,
            eventExpireTime = 30.days
        ).apply {
            optionsBuilder?.invoke(this)
        }.build()
        return KottageStorageImpl(
            name,
            options.json ?: json,
            options,
            this.options,
            databaseManager,
            environment.calendar ?: KottageSystemCalendar(),
            { databaseManager.compact() },
            dispatcher
        )
    }

    /**
     * get KottageEventFlow
     */
    fun eventFlow(afterUnixTimeMillisAt: Long? = null): KottageEventFlow {
        return databaseManager.eventFlow(afterUnixTimeMillisAt)
    }

    /**
     * Evict expired caches and
     * Optimize Database to minimum size.
     */
    suspend fun compact() {
        databaseManager.compact(true)
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

    data class DatabaseFiles(
        /**
         * SQLite DB file: "${name}.db"
         */
        val databaseFile: String,
        /**
         * SQLite DB WAL file: "${databaseFile}-wal"
         */
        val walFile: String,
        /**
         * SQLite DB SHM file: "${databaseFile}-shm"
         */
        val shmFile: String
    )
}

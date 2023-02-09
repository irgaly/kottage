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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days

/**
 * Kotlin KVS Kottage
 *
 * @param scope Kottage instance's lifecycle scope. This Kottage instance is automatically closed at the end of the scope.
 * @throws IllegalArgumentException name contains file separator
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class Kottage(
    val name: String,
    val directoryPath: String,
    val environment: KottageEnvironment,
    val scope: CoroutineScope,
    val json: Json = Json,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
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
        KottageDatabaseManager(name, directoryPath, options, environment, scope, dispatcher)

    /**
     * Simple KottageEvent Flow
     * This is a simple hot flow.
     *
     * see also [eventFlow]
     */
    val simpleEventFlow: Flow<KottageEvent> =
        databaseManager.eventFlow.map { KottageEvent.from(it) }

    /**
     * Database Connection is closed or not.
     *
     * If Kottage is closed, all database operation of this kottage instance will be fail.
     * A closed Kottage instance cannot be reused.
     */
    val closed: Boolean get() = databaseManager.databaseConnectionClosed

    init {
        require(!name.contains(Files.separator)) { "name contains separator: $name" }
        checkNotNull(scope.coroutineContext[Job]) {
            "CoroutineScope does not have Job context. for example, GlobalScope has no Job, so GlobalScope is not allowed to pass to Kottage."
        }.invokeOnCompletion {
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                // 親 scope は終了しているため、GlobalScope で close 処理を実行する
                close()
            }
        }
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

    /**
     * Close Database connection
     *
     * If this Kottage instance is already closed, this method do nothing.
     *
     * Kottage instance is automatically closed at the end of CoroutineScope that is passed with constructor parameter.
     * So you don't have to call close() manually, if the scope's lifecycle equals Kottage instance's lifecycle.
     */
    suspend fun close() {
        databaseManager.closeDatabaseConnection()
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

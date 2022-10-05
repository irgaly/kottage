package io.github.irgaly.kottage.internal

import io.github.irgaly.kottage.KottageEntry
import io.github.irgaly.kottage.KottageEvent
import io.github.irgaly.kottage.KottageEventFlow
import io.github.irgaly.kottage.KottageList
import io.github.irgaly.kottage.KottageListOptions
import io.github.irgaly.kottage.KottageOptions
import io.github.irgaly.kottage.KottageStorage
import io.github.irgaly.kottage.KottageStorageOptions
import io.github.irgaly.kottage.internal.encoder.Encoder
import io.github.irgaly.kottage.internal.encoder.encodeItem
import io.github.irgaly.kottage.platform.KottageCalendar
import io.github.irgaly.kottage.strategy.KottageStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.reflect.KType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

internal class KottageStorageImpl(
    override val name: String,
    json: Json,
    override val options: KottageStorageOptions,
    private val kottageOptions: KottageOptions,
    private val databaseManager: KottageDatabaseManager,
    private val calendar: KottageCalendar,
    private val onCompactionRequired: suspend () -> Unit,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : KottageStorage {
    private val encoder = Encoder(json)

    private val strategy: KottageStrategy = options.strategy

    private val itemType: String = name

    private suspend fun itemRepository() = databaseManager.itemRepository.await()
    private suspend fun itemListRepository() = databaseManager.itemListRepository.await()
    private suspend fun itemEventRepository() = databaseManager.itemEventRepository.await()
    private suspend fun operator() = databaseManager.operator.await()

    @OptIn(DelicateCoroutinesApi::class)
    private val storageOperator = GlobalScope.async(dispatcher, CoroutineStart.LAZY) {
        databaseManager.getStorageOperator(this@KottageStorageImpl)
    }

    override val defaultExpireTime: Duration? get() = options.defaultExpireTime

    override suspend fun <T : Any> get(key: String, type: KType): T {
        return getOrNullInternal(key, type)
            ?: throw NoSuchElementException("key = $key, storage name = $name")
    }

    override suspend fun <T : Any> getOrNull(key: String, type: KType): T? {
        return getOrNullInternal(key, type)
    }

    /**
     * @throws ClassCastException when decode failed
     * @throws SerializationException when json decode failed
     */
    private suspend fun <T : Any> getOrNullInternal(key: String, type: KType): T? =
        withContext(dispatcher) {
            val operator = operator()
            val storageOperator = storageOperator.await()
            val now = calendar.nowUnixTimeMillis()
            var compactionRequired = false
            val item = databaseManager.transactionWithResult {
                compactionRequired =
                    operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
                storageOperator.getOrNull(key, now)?.also {
                    strategy.onItemRead(key, itemType, now, operator)
                }
            }
            if (compactionRequired) {
                onCompactionRequired()
            }
            item?.let { encoder.decode(it, type) }
        }

    override suspend fun <T : Any> getEntry(key: String, type: KType): KottageEntry<T> {
        return getEntryOrNullInternal(key, type)
            ?: throw NoSuchElementException("key = $key, storage name = $name")
    }

    override suspend fun <T : Any> getEntryOrNull(key: String, type: KType): KottageEntry<T>? {
        return getEntryOrNullInternal(key, type)
    }

    private suspend fun <T : Any> getEntryOrNullInternal(
        key: String,
        type: KType
    ): KottageEntry<T>? = withContext(dispatcher) {
        val operator = operator()
        val storageOperator = storageOperator.await()
        val now = calendar.nowUnixTimeMillis()
        var compactionRequired = false
        val item = databaseManager.transactionWithResult {
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
            storageOperator.getOrNull(key, now)?.also {
                strategy.onItemRead(key, itemType, now, operator)
            }
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        item?.let { KottageEntry(it, type, encoder) }
    }

    override suspend fun exists(key: String): Boolean = withContext(dispatcher) {
        val now = calendar.nowUnixTimeMillis()
        val item = itemRepository().get(key, itemType)
        (item?.isAvailable(now) ?: false)
    }

    override suspend fun <T : Any> put(key: String, value: T, type: KType, expireTime: Duration?) =
        withContext(dispatcher) {
            val operator = operator()
            val storageOperator = storageOperator.await()
            val now = calendar.nowUnixTimeMillis()
            val item =
                encoder.encodeItem(this@KottageStorageImpl, key, value, type, now, expireTime)
            var compactionRequired = false
            databaseManager.transaction {
                storageOperator.upsertItem(item, now)
                compactionRequired =
                    operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
            }
            if (compactionRequired) {
                onCompactionRequired()
            }
            databaseManager.onEventCreated()
        }

    override suspend fun remove(key: String): Boolean = withContext(dispatcher) {
        val itemRepository = itemRepository()
        val operator = operator()
        val storageOperator = storageOperator.await()
        val now = calendar.nowUnixTimeMillis()
        var compactionRequired = false
        var eventCreated = false
        val exists = databaseManager.transactionWithResult {
            val exists = itemRepository.exists(key, itemType)
            if (exists) {
                storageOperator.deleteItem(key = key, now = now) {
                    eventCreated = true
                }
            }
            compactionRequired =
                operator.getAutoCompactionNeeded(now, kottageOptions.autoCompactionDuration)
            exists
        }
        if (compactionRequired) {
            onCompactionRequired()
        }
        if (eventCreated) {
            databaseManager.onEventCreated()
        }
        exists
    }

    override suspend fun removeAll(key: String): Unit = withContext(dispatcher) {
        val itemRepository = itemRepository()
        val operator = operator()
        val storageOperator = storageOperator.await()
        val now = calendar.nowUnixTimeMillis()
        var eventCreated = false
        databaseManager.transaction {
            itemRepository.getAllKeys(itemType) { key ->
                storageOperator.deleteItem(key = key, now = now) {
                    eventCreated = true
                }
            }
        }
        // TODO: remove 同様に Compaction 判定と実行
        if (eventCreated) {
            databaseManager.onEventCreated()
        }
    }

    override suspend fun compact() = withContext(dispatcher) {
        val operator = operator()
        val now = calendar.nowUnixTimeMillis()
        databaseManager.transaction {
            operator.evictCaches(now, itemType)
            operator.evictEvents(now, itemType)
        }
    }

    override suspend fun clear() = withContext(dispatcher) {
        val itemRepository = itemRepository()
        val itemEventRepository = itemEventRepository()
        databaseManager.transaction {
            itemRepository.deleteAll(itemType)
            itemEventRepository.deleteAll(itemType)
            itemRepository.deleteStats(itemType)
        }
    }

    override suspend fun getEvents(afterUnixTimeMillisAt: Long, limit: Long?): List<KottageEvent> =
        withContext(dispatcher) {
            val operator = operator()
            databaseManager.transactionWithResult {
                operator.getEvents(
                    afterUnixTimeMillisAt = afterUnixTimeMillisAt,
                    itemType = itemType,
                    limit = limit
                ).map {
                    KottageEvent.from(it)
                }
            }
        }

    override fun eventFlow(afterUnixTimeMillisAt: Long?): KottageEventFlow {
        return databaseManager.eventFlow(afterUnixTimeMillisAt, itemType)
    }

    override fun list(
        name: String,
        optionsBuilder: (KottageListOptions.Builder.() -> Unit)?
    ): KottageList {
        val options = KottageListOptions.Builder(
            itemExpireTime = 30.days
        ).apply {
            optionsBuilder?.invoke(this)
        }.build()
        return KottageListImpl(
            name,
            this,
            this.options.strategy,
            encoder,
            options,
            kottageOptions,
            databaseManager,
            calendar,
            onCompactionRequired,
            dispatcher
        )
    }
}

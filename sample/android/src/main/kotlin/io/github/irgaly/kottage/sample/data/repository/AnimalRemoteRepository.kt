package io.github.irgaly.kottage.sample.data.repository

import android.util.Log
import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageListEntry
import io.github.irgaly.kottage.kottageListValue
import io.github.irgaly.kottage.sample.model.Animal
import io.github.serpro69.kfaker.faker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class AnimalRemoteRepository(
    kottage: Kottage
) {
    private val list = kottage.cache(
        "AnimalRemoteRepository"
    ).list("database")

    private fun generate(): List<Animal> {
        val faker = faker {}
        return (1..300).map { index ->
            Animal(
                UUID.randomUUID().toString(),
                index.toLong(),
                faker.animal.name().replaceFirstChar { it.uppercase() }
            )
        }
    }

    suspend fun regenerate() {
        list.clear()
        val animals = generate()
        list.addAll(
            animals.map {
                kottageListValue(it.id, it)
            }
        )
    }

    suspend fun load(): List<Animal> {
        return list.getPageFrom(
            positionId = null,
            pageSize = null
        ).items.map(KottageListEntry::value)
    }

    suspend fun loadPrevious(lastId: String, pageSize: Int): Page? = withContext(Dispatchers.IO) {
        Log.d(
            ":sample:android",
            "=> AnimalRemoteRepository.loadPrevious(lastId = $lastId, pageSize = $pageSize)"
        )
        val database = load()
        // loading delay for demo
        delay(2.seconds)
        val lastItemIndex = database.indexOfFirst {
            (it.id == lastId)
        }
        val index =
            if (0 <= lastItemIndex) (lastItemIndex - 1)
            else null
        if (index != null && 0 <= index) {
            val firstIndex = (index - pageSize + 1).coerceAtLeast(0)
            val items = database.slice(firstIndex..index)
            if (items.isNotEmpty()) {
                Page(items, (0 < firstIndex))
            } else null
        } else null
    }

    suspend fun loadNext(lastId: String?, pageSize: Int): Page? = withContext(Dispatchers.IO) {
        Log.d(
            ":sample:android",
            "=> AnimalRemoteRepository.loadNext(lastId = $lastId, pageSize = $pageSize)"
        )
        val database = load()
        // loading delay for demo
        delay(2.seconds)
        val index = if (lastId == null) 0 else {
            val lastItemIndex = database.indexOfFirst {
                (it.id == lastId)
            }
            if (0 <= lastItemIndex) (lastItemIndex + 1) else null
        }
        if (index != null) {
            val lastIndex = (index + pageSize - 1).coerceAtMost(database.lastIndex)
            val items = database.slice(index..lastIndex)
            if (items.isNotEmpty()) {
                Page(items, (lastIndex < database.lastIndex))
            } else null
        } else null
    }

    data class Page(
        val items: List<Animal>,
        val hasNext: Boolean
    )
}

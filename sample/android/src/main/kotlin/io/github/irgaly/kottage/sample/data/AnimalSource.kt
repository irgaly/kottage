package io.github.irgaly.kottage.sample.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.irgaly.kottage.sample.model.Animal
import io.github.serpro69.kfaker.faker
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class AnimalSource: PagingSource<String, Animal>() {
    private val faker = faker{}
    private val keys = mutableMapOf<String, Pair<String?, String?>>()
    override suspend fun load(params: LoadParams<String>): LoadResult<String, Animal> {
        delay(2.seconds)
        val key = params.key
        val pageKey = key ?: ""
        val prevKey = keys[pageKey]?.first
        val nextKey = keys[pageKey]?.second ?: UUID.randomUUID().toString()
        keys[nextKey] = Pair(nextKey, null)
        keys[pageKey] = Pair(prevKey, nextKey)
        return LoadResult.Page(
            data = (0..10).map {
                Animal(faker.animal.name().replaceFirstChar { it.uppercase() })
            },
            prevKey = prevKey,
            nextKey = nextKey
        )
    }

    override fun getRefreshKey(state: PagingState<String, Animal>): String? {
        return ""
    }
}

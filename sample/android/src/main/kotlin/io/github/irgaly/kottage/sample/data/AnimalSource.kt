package io.github.irgaly.kottage.sample.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageListDirection
import io.github.irgaly.kottage.sample.model.Animal
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds

class AnimalSource(
    kottage: Kottage
) : PagingSource<String, AnimalSource.Item>() {
    private val list = kottage.cache(
        "animal"
    ).list("animal_list")

    companion object {
        fun getInvalidationFlow(kottage: Kottage): Flow<Unit> {
            @OptIn(FlowPreview::class)
            return kottage.cache(
                "animal"
            ).eventFlow().filter { event ->
                // TODO: KottageList.eventFlow() が実装されたら置き換える
                (event.itemType == "animal" || event.listType == "animal_list")
            }
                // wait consecutive events, ex: KottageList.addAll()
                .debounce(100.milliseconds)
                .map {}
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Item> {
        val direction = when (params) {
            is LoadParams.Refresh, is LoadParams.Append -> KottageListDirection.Forward
            is LoadParams.Prepend -> KottageListDirection.Backward
        }
        val page = list.getPageFrom(
            positionId = params.key,
            pageSize = params.loadSize.toLong(),
            direction = direction
        ).let { result ->
            if (params.key != null && result.isEmpty) {
                // params.key is obsolete, so fallback to load first page
                list.getPageFrom(
                    positionId = null,
                    pageSize = params.loadSize.toLong(),
                    direction = direction
                )
            } else result
        }
        return LoadResult.Page(
            data = page.items.map {
                Item(
                    positionId = it.positionId,
                    animal = it.value()
                )
            },
            prevKey = page.previousPositionId,
            nextKey = page.nextPositionId
        )
    }

    override fun getRefreshKey(state: PagingState<String, Item>): String? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.let { page ->
                // get closest page's first item's positionId
                page.data.firstOrNull()?.positionId
            }
        }
    }

    /**
     * clear local kottage data
     */
    suspend fun clear() {
        list.clear()
        // TODO: list.clear() / list.dropList() に分ければ invalidate は不要
        invalidate()
    }

    data class Item(
        val positionId: String,
        val animal: Animal
    )
}

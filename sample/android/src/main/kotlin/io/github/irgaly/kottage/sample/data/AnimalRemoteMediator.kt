package io.github.irgaly.kottage.sample.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.kottageListValue
import io.github.irgaly.kottage.sample.data.repository.AnimalRemoteRepository
import io.github.irgaly.kottage.sample.model.Animal
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class AnimalRemoteMediator(
    kottage: Kottage,
    private val animalRemoteRepository: AnimalRemoteRepository,
    private val pageSize: Int
) : RemoteMediator<String, AnimalSource.Item>() {
    private val list = kottage.cache(
        "animal"
    ).list("animal_list")

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<String, AnimalSource.Item>
    ): MediatorResult {
        return try {
            var inconsistentRequest = false
            val page = when (loadType) {
                LoadType.PREPEND -> {
                    val lastId = state.firstItemOrNull()?.animal?.id
                    if (lastId != null && lastId == list.getFirst()?.value<Animal>()?.id) {
                        // load data only if first items on UI and database are same.
                        animalRemoteRepository.loadPrevious(
                            lastId = lastId,
                            pageSize = pageSize
                        )
                    } else {
                        // this loading request is old, stop loading
                        inconsistentRequest = true
                        null
                    }
                }

                LoadType.REFRESH -> {
                    animalRemoteRepository.loadNext(
                        lastId = null,
                        pageSize = pageSize
                    )
                }

                LoadType.APPEND -> {
                    val lastId = state.lastItemOrNull()?.animal?.id
                    if (lastId != null && lastId == list.getLast()?.value<Animal>()?.id) {
                        // load data only if last items on UI and database are same.
                        animalRemoteRepository.loadNext(
                            lastId = lastId,
                            pageSize = pageSize
                        )
                    } else {
                        // this loading request is old, stop loading
                        inconsistentRequest = true
                        null
                    }
                }
            }
            if (loadType == LoadType.REFRESH) {
                // clear local data when refresh is succeeded
                list.clear()
            }
            // store data to kottage
            page?.items?.map { animal ->
                kottageListValue(
                    key = animal.id,
                    value = animal
                )
            }?.also {
                when (loadType) {
                    LoadType.PREPEND -> list.addAllFirst(it)
                    LoadType.REFRESH, LoadType.APPEND -> list.addAll(it)
                }
            }
            if (inconsistentRequest) {
                // abort loading remote data when there is a mismatch between UI page and local database
                MediatorResult.Success(
                    endOfPaginationReached = false
                )
            } else {
                MediatorResult.Success(
                    endOfPaginationReached = (page == null)
                )
            }
        } catch (error: IOException) {
            // an example error handling: network access failed
            MediatorResult.Error(error)
        }
    }
}

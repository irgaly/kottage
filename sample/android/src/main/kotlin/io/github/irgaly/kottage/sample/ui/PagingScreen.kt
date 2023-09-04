package io.github.irgaly.kottage.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.irgaly.kottage.Kottage
import io.github.irgaly.kottage.KottageEnvironment
import io.github.irgaly.kottage.platform.contextOf
import io.github.irgaly.kottage.sample.data.AnimalRemoteMediator
import io.github.irgaly.kottage.sample.data.AnimalSource
import io.github.irgaly.kottage.sample.data.repository.AnimalRemoteRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PagingScreen(
    kottage: Kottage,
    animalRemoteRepository: AnimalRemoteRepository
) {
    val scope = rememberCoroutineScope()
    var animalSource by remember {
        mutableStateOf<AnimalSource?>(null)
    }
    val items = remember(kottage, scope) {
        var source: PagingSource<*, *>? = null
        AnimalSource.getInvalidationFlow(kottage).onEach {
            // The dataset changed, then reload items from KottageList
            source?.invalidate()
            source = null
        }.launchIn(scope)
        @OptIn(ExperimentalPagingApi::class)
        Pager(
            config = PagingConfig(
                pageSize = 30
            ),
            remoteMediator = AnimalRemoteMediator(
                kottage = kottage,
                animalRemoteRepository = animalRemoteRepository,
                pageSize = 30
            )
        ) {
            AnimalSource(kottage = kottage).also {
                source = it
                animalSource = it
            }
        }
    }.flow.collectAsLazyPagingItems()
    val refreshing = (items.loadState.refresh is LoadState.Loading)
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            items.refresh()
        }
    )
    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                @Composable
                fun LoadState.textColor(): Color {
                    return when (this) {
                        is LoadState.NotLoading -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        is LoadState.Loading -> MaterialTheme.colorScheme.onSurface
                        is LoadState.Error -> MaterialTheme.colorScheme.error
                    }
                }
                Column(
                    Modifier
                        .width(230.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text("CombinedLoadStates", style = MaterialTheme.typography.titleSmall)
                    Column(Modifier.padding(start = 8.dp)) {
                        Row {
                            Text("refresh: ")
                            Text(
                                items.loadState.refresh::class.simpleName.toString(),
                                color = items.loadState.refresh.textColor()
                            )
                        }
                        Row {
                            Text("prepend: ")
                            Text(
                                items.loadState.prepend::class.simpleName.toString(),
                                color = items.loadState.prepend.textColor()
                            )
                        }
                        Row {
                            Text("append: ")
                            Text(
                                items.loadState.append::class.simpleName.toString(),
                                color = items.loadState.append.textColor()
                            )
                        }
                    }
                }
                Column(
                    Modifier
                        .width(230.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text("Source LoadStates", style = MaterialTheme.typography.titleSmall)
                    Column(Modifier.padding(start = 8.dp)) {
                        Row {
                            Text("refresh: ")
                            Text(
                                items.loadState.source.refresh::class.simpleName.toString(),
                                color = items.loadState.source.refresh.textColor()
                            )
                        }
                        Row {
                            Text("prepend: ")
                            Text(
                                items.loadState.source.prepend::class.simpleName.toString(),
                                color = items.loadState.source.prepend.textColor()
                            )
                        }
                        Row {
                            Text("append: ")
                            Text(
                                items.loadState.source.append::class.simpleName.toString(),
                                color = items.loadState.source.append.textColor()
                            )
                        }
                    }
                }
                Column(
                    Modifier
                        .width(230.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text("Mediator LoadStates", style = MaterialTheme.typography.titleSmall)
                    Column(Modifier.padding(start = 8.dp)) {
                        val mediator = items.loadState.mediator
                        Row {
                            Text("refresh: ")
                            Text(
                                mediator?.let { it.refresh::class.simpleName } ?: "-",
                                color = mediator?.refresh?.textColor()
                                    ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Row {
                            Text("prepend: ")
                            Text(
                                mediator?.let { it.prepend::class.simpleName } ?: "-",
                                color = mediator?.prepend?.textColor()
                                    ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Row {
                            Text("append: ")
                            Text(
                                mediator?.let { it.append::class.simpleName } ?: "-",
                                color = mediator?.append?.textColor()
                                    ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(onClick = {
                    items.refresh()
                }) {
                    Text("items.refresh()")
                }
                Button(onClick = {
                    animalSource?.invalidate()
                }) {
                    Text("animalSource.invalidate()")
                }
                Button(onClick = {
                    scope.launch {
                        animalSource?.clear()
                    }
                }) {
                    Text("KottageList.removeAll()")
                }
                Button(onClick = {
                    scope.launch {
                        animalRemoteRepository.regenerate()
                        items.refresh()
                    }
                }) {
                    Text("AnimalRemoteRepository.regenerate()")
                }
            }
        }
    ) { padding ->
        Box(Modifier.pullRefresh(pullRefreshState)) {
            LazyColumn(
                Modifier
                    .fillMaxSize(),
                contentPadding = padding
            ) {
                if (items.loadState.prepend is LoadState.Loading) {
                    item("prepend") {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(24.dp)
                            )
                        }
                    }
                }
                items(
                    count = items.itemCount,
                    key = items.itemKey { it.animal.id }
                ) { index ->
                    with(checkNotNull(items[index])) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${"%4d".format(animal.index)} : ${animal.name} - position = ${
                                    positionId.take(10)
                                }..."
                            )
                        }
                    }
                }
                if (items.loadState.append is LoadState.Loading) {
                    item("append") {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                Modifier
                    .padding(padding)
                    .align(Alignment.TopCenter)
            )
        }
    }

}

@Preview
@Composable
private fun PagingScreenPreview() {
    MaterialTheme {
        val kottage = Kottage(
            name = "preview_dummy",
            directoryPath = "preview_dummy",
            KottageEnvironment(contextOf(LocalContext.current)),
            rememberCoroutineScope()
        )
        PagingScreen(
            kottage = kottage,
            animalRemoteRepository = AnimalRemoteRepository(kottage = kottage)
        )
    }
}

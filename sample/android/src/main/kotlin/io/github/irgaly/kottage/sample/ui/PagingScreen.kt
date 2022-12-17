package io.github.irgaly.kottage.sample.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import io.github.irgaly.kottage.sample.data.AnimalSource

@Composable
fun PagingScreen() {
    val items = remember {
        Pager(
            PagingConfig(pageSize = 10)
        ) {
            AnimalSource()
        }
    }.flow.collectAsLazyPagingItems()
    Surface {
        LazyColumn(
            Modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { index, item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${"%4d".format(index)} : ${item?.name}")
                }
            }
        }
    }

}

@Preview
@Composable
private fun PagingScreenPreview() {
    MaterialTheme {
        PagingScreen()
    }
}

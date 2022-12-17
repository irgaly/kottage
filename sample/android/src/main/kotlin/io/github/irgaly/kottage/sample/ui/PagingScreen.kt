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
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.serpro69.kfaker.faker

@Composable
fun PagingScreen() {
    val faker = remember { faker{}  }
    Surface {
        LazyColumn(
            Modifier.fillMaxSize()
        ) {
            items(10) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(faker.animal.name().replaceFirstChar { it.uppercase() })
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

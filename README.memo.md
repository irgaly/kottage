# Kottage memo

調べること:

* 以下のそれぞれの、アプリ保存ディレクトリ、キャッシュディレクトリの取得方法:
  * Android
  * iOS
  * Linux
  * macOS
  * Windows

以下は設計メモ:

* ログはアプリ側からDIする
* storage.get(Key("key", typeOf(Int))) くらいは用意してもいいかも

---

# List Cache

for AndroidX Paging

```kotlin
data class Item(val positionId: String)

class MyPagingSource(
  kottage: Kottage
) : PagingSource<String, Item>() {
  val cache = kottage.cache("items")
  val list: KottageList = cache.list("my_page")
  override fun getRefreshKey(state: PagingState<String, Item>): String? {
    val page = state.closestPageToPosition(state.anchorPosition!!)!!
    return page.data.firstOrNull()?.positionId
  }

  override suspend fun load(params: LoadParams<String>): LoadResult<String, Item> {
    val key = params.key
    val page: KottageListPage = when (params) {
      is LoadParams.Refresh -> {
        itemsList.getPageFrom(positionId = key, pageSize = 20L, direction = KottageListDirection.Forward)
      }

      is LoadParams.Append -> {
        itemsList.getPageFrom(positionId = key, pageSize = 20L, direction = KottageListDirection.Forward)
      }

      is LoadParams.Prepend -> {
        itemsList.getPageFrom(positionId = key, pageSize = 20L, direction = KottageListDirection.Backward)
      }
    }
    return LoadResult.Page(
      data = page.items.map { it.value<Item>() },
      prevKey = page.previousPositionId,
      nextKey = page.nextPositionId
    )
  }
}
```

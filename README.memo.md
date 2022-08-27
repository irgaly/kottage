# Kottage

調べること:

* SQLDelight のマイグレーションの方法
* 以下のそれぞれの、アプリ保存ディレクトリ、キャッシュディレクトリの取得方法:
  * Android
  * iOS
  * Linux
  * macOS
  * Windows
* Kotlin MPP ライブラリ maven publish

以下は設計メモ:

* ログはアプリ側からDIする

---

TODO:

* 任意の暗号化ロジックを差し込めるようにする
* Evicted event は生成しないほうがよさそう？
  * GC のタイミングで大量のイベントが発生するため
* ある程度 item を追加したときに expired アイテムのクリーンアップをする

機能

* LRU KVS cache
  * Data type ごとに上限個数を設定
  * データサイズは計算しない
  * 書き込みのタイミングで clean する
  * Key = String
  * null オブジェクト + Meta 情報アイテムに対応する
  * Last Hit
    * 揮発済みの cache を 1回だけ取り出せる
* 構造
  * Storage
    * 上限個数設定
    * Event
      * Item の CRUD を通知する
    * リスト機能 (複数可能)
      * Linked List + External Key
        * 並び替えにも対応する (優先度低)
      * Event
        * Item の CRUD を通知する

こう使いたい

```kotlin

// Create Instance
val kottage = Kottage("kottage-name", dir, json) // SQLite ファイル名, ディレクトリ名
val storage: KottageStorage = kottage.storage("storage-name", kottageStorage {
  // kottage.cache() だとキャッシュ専用のストレージを得る
  strategy = KottageLruStrategy(
    maxEntryCount = 1024 // counts
  )
  strategy = KottageFifoStrategy(
    maxEntryCount = 1024 // counts
  )
  strategy = KottageKvsStrategy() // 追い出しなし
  defaultExpireTime = 1.month // nullable
  autoCompact = true
  withLastHit = true // 有効期限切れのエントリーを返すか
  json = Json
})

// Save Cache
storage.put("key", "cache")
storage.put("key", "cache", 1000.seconds) // expire time
storage.put("key", "cache", storage.defaultExpireTime)

// Read Cache
val value: KottageEntry<String> = storage.read<String>("key") // Meta 要素あり
val value: KottageEntry<ByteArray> = storage.read<ByteArray>("key")
val value: String? = storage.getOrNull("key") // 値のみ
val contains: Boolean = storage.contains("key")

// List View
val list = storage.list("list-key")

// Save List
list.add()

// Delete Cache
storage.remove("key")

// Clean Cache
storage.compact()

// Delete Storage
storage.clear()

// Delete Kottage
Kottage.clear()

```

# Kotlin-KVS

Kotlin Simple Cache Storage for Kotlin Multiplatform

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
val kkvs = Kkvs("kkvs-name") // SQLite ファイル名
val storage: KkvsStorage = kkvs.storage("storage-name", kkvsStorage {
  // kkvs.cache() だとキャッシュ専用のストレージを得る
  strategy = KkvsLruStrategy(
    maxEntryCount = 1024 // counts
  )
  strategy = KkvsFifoStrategy(
    maxEntryCount = 1024 // counts
  )
  strategy = KkvsKvsStrategy() // 追い出しなし
  defaultExpireTime = 1.month // nullable
  autoClean = true
  withLastHit = true // 有効期限切れのエントリーを返すか
})

// Save Cache
storage.put("key", "cache")
storage.put("key", "cache", 1000.seconds) // expire time
storage.put("key", "cache", storage.defaultExpireTime)

// Read Cache
val value: KkvsEntry<String> = storage.read<String>("key") // Meta 要素あり
val value: KkvsEntry<ByteArray> = storage.read<ByteArray>("key")
val value: String? = storage.get("key") // 値のみ

// List View
val list = storage.list("list-key")

// Save List
list.add()

// Delete Cache
storage.remove("key")

// Clean Cache
storage.clean()

// Delete Storage
storage.clear()

// Delete Kkvs
kkvs.clear()

```

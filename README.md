# Kottage

Kotlin Multiplatform Key-Value Store Local Cache Storage for Single Source of Truth.

# Features

* A Kotlin Multiplatform library
* Key-Value Store with no schemas, values are stored to SQLite
* Observing events of item updates as Flow
* Cache Expiration
    * Cache Eviction Strategies:
        * Expiration Time
        * FIFO Strategy
        * LRU Strategy
* KVS Cache mode
    * Expired items are evicted automatically
* KVS Storage mode
    * no item expiration
* List structures for Paging are supported
* Support primitive values and `@Serializable` classes

# Requires

* [New memory manager](https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md)
  enabled with Kotlin/Native platform.
* SQLite3 dynamic link library on runtime environment
    * Windows needs sqlite3.dll on library path or bundled with your application.
        * Download from https://www.sqlite.org/download.html > sqlite-dll-win64-x64-3410200.zip
    * Linux needs sqlite3 package on system.
    * macOS and iOS platform has sqlite3 library, so you don't have to bundle it.
    * Android has SQLiteHelper class, no dynamic libraries are needed.
    * JVM has JDBC SQLite driver, no dynamic libraries are needed.
    * Nodejs bundles SQLite3 binary, no dynamic libraries are needed.

# Usage

## Setup

Add Kottage as gradle dependency.

### Kotlin Multiplatform:

`build.gradle.kts`

```kotlin
// For Kotlin Multiplatform:
plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            implementation("io.github.irgaly.kottage:kottage:1.7.0")
        }
    }
    // ...
}
```

### Android or JVM without Kotlin Multiplatform:

`build.gradle.kts`

```kotlin
// For Kotlin/JVM or Kotlin/Android without Kotlin Multiplatform:
plugins {
    id("com.android.application")
    kotlin("android")
    // kotlin("jvm") // for JVM Application
}

dependencies {
    // You can use as JVM library directly
    implementation("io.github.irgaly.kottage:kottage:1.7.0")
    // ...
}
```

## Use Kottage

Use Kottage as KVS cache or KVS storage.

First, get a Kottage instance. Even though you can use Kottage instance as a singleton, multiple
Kottage instances creation is allowed. Kottage instances and methods are thread safe.

```kotlin
import io.github.irgaly.kottage.platform.contextOf

// directory path string for SQLite file
// For example:
// * Android File Directory: context.getFilesDir().path
// * Android Cache Directory: context.getCacheDir().path
val databaseDirectory: String = ...
val kottageEnvironment: KottageEnvironment = KottageEnvironment(
    context = contextOf(context) // for Android, set a KottageContext with Android Context object
    //context = KottageContext() // for other platforms, set an empty KottageContext
)
// Initialize with Kottage database information.
val kottage: Kottage = Kottage(
    name = "kottage-store-name", // This will be database file name
    directoryPath = databaseDirectory,
    environment = kottageEnvironment,
    scope = scope, // This kottage instance will be automatically close on this CoroutineScope completion
    json = Json.Default // kotlinx.serialization's json object
)
```

Then, use it as KVS Cache.

```kotlin
import kotlin.time.Duration.Companion.days

// Open Kottage database as cache mode
val cache: KottageStorage = kottage.cache("timeline_item_cache") {
    // There are some options
    strategy = KottageFifoStrategy(maxEntryCount = 1000) // default strategy in cache mode
    //strategy = KottageLruStrategy(maxEntryCount = 1000) // LRU cache strategy
    defaultExpireTime = 30.days // cache item expiration time in kotlin.time.Duration
}

// Kottage's data accessing methods (get, put...) are suspending function
// These items will be expired and automatically deleted after 30 days (defaultExpireTime) elapsed
cache.put("item1", "item1 value")
cache.put("item2", 42)
cache.put("item3", true)

val value1: String = cache.get<String>("item1")
val value2: Int = cache.get<Int>("item2")
val value3: Boolean = cache.get<Boolean>("item3")
cache.exists("item4") // => false
cache.getOrNull<String>("item4") // => null

// 30 days later... these items are expired
cache.get<String>("item1") // throws NoSuchElementException
cache.getOrNull<String>("item1") // => null
cache.exists("item1") // => false
```

Use it as KVS Storage with no expiration.

```kotlin
// Open Kottage database as storage mode
val storage: KottageStorage = kottage.storage("app_configs")

// Kottage's data accessing methods (get, put...) are suspending function
// These items has no expiration
storage.put("item1", "item1 value")
storage.put("item2", 42)
storage.put("item3", true)

val value1: String = storage.get<String>("item1")
val value2: Int = storage.get<Int>("item2")
val value3: Boolean = storage.get<Boolean>("item3")
storage.exists("item4") // => false
storage.getOrNull<String>("item4") // => null
```

### Property Delegation

KottageStorage provides property delegate.

```kotlin
val storage: KottageStorage = kottage.storage("app_configs")

val myConfig: String by storage.property { "default value" }
val myConfigNullable: String? by storage.nullableProperty()

myConfig.write("value")
val config: String = myConfig.read()
```

For example, this is strictly typed data access class:

```kotlin
class AppConfiguration(kottage: Kottage) {
    private val storage: KottageStorage = kottage.storage("app_configs")
    val myConfig: String by storage.property { "default value" }
    val myConfigNullable: String? by storage.nullableProperty()
}

val configuration: AppConfiguration = AppConfiguration(kottage)
configuration.myConfig.write("value")
val config: String = configuration.myConfig.read()
```

### List / Paging

Kottage has a List feature for make Paging UIs and for Single Source of Truth.

```kotlin
import io.github.irgaly.kottage.kottageListValue

val cache: KottageStorage = kottage.cache("timeline_item_cache")
val list: KottageList = cache.list("timeline_list")

// KottageList is an interface of KottageStorage that supports List operations.

// Add List Items
list.add("item_id_1", TimelineItem("item_id_1", ...))
list.addAll(
    listOf(
        kottageListValue("item_id_2", TimelineItem("item_id_2", ...)),
        kottageListValue("item_id_3", TimelineItem("item_id_3", ...)),
        kottageListValue("item_id_4", TimelineItem("item_id_4", ...)),
        kottageListValue("item_id_5", TimelineItem("item_id_5", ...))
    )
)

// The items are stored in "timeline_item_cache" KottageStorage
cache.exists("item_id_1") // => true

// You can update items directly
cache.put("item_id_1", TimelineItem("item_id_1", otherValue = ...))

// Get as Page
val page0: KottageListPage = list.getPageFrom(positionId = null, pageSize = 2)
val page1: KottageListPage = list.getPageFrom(positionId = (page0.nextPositionId), pageSize = 2)
val page2: KottageListPage = list.getPageFrom(positionId = (page1.nextPositionId), pageSize = 2)

page0.items // => List<KottageListEntry>
page0.items.map { it.value<TimelineItem>() }
  // => [TimelineItem("item_id_1", otherValue = ...), TimelineItem("item_id_2", ...)]
page1.items.map { it.value<TimelineItem>() }
  // => [TimelineItem("item_id_3", ...), TimelineItem("item_id_4", ...)]
page2.items.map { it.value<TimelineItem>() }
  // => [TimelineItem("item_id_5", ...)]
page2.hasNext // => false

// KottageList is a Linked List.
val entry1: KottageListEntry? = list.getFirst()
val entry2: KottageListEntry? = list.get(checkNotNull(entry1.nextPositionId))
val entry3: KottageListEntry? = list.get(checkNotNull(entry2.nextPositionId))
// convenience method to get an entry by index
val entry4: KottageListEntry? = list.getByIndex(3)

entry1?.value<TimelineItem>() // => TimelineItem("item_id_1", otherValue = ...)
```

### Serialization

Kottage can store and restore Serializable classes.

```kotlin
@Serializable
data class MyData(val myValue: Int)

val data: MyData = MyData(42)
val list: List<String> = listOf("item1", "item2") // List<String> is Serializable
val cache: KottageStorage = kottage.cache("my_data_cache")
cache.put("item1", data)
cache.put("item2", list)
val storedData: MyData = cache.get<MyData>("item1")
val storedList: List<String> = cache.get<List<String>>("item2")
```

### Type mismatch error

Store and restore works correctly with same type. It throws ClassCastException if restore with wrong
types.

```kotlin
val cache: KottageStorage = kottage.cache("type_items")
cache.put("item1", 0) // Store as Number (= SQLite Number = Long, Int, Short, Byte or Boolean)
cache.put("item2", "strings") // Store as String
cache.get<String>("item1") // throws ClassCastException
cache.get<Int>("item2") // throws ClassCastException
```

Serializable types are stored as String. It throws SerializationException if restore with wrong
types.

```kotlin
@Serializable
data class Data(val data: Int)

@Serializable
data class Data2(val data2: Int)

val cache: KottageStorage = kottage.cache("type_items")
cache.put("data", Data(42))
cache.get<String>("data") // => "{\"data\":42}"
cache.get<Data2>("data") // throws SerializationException
```

### Event Observing

Kottage supports observing events of item updates for implementing Single Source of Truth.

```kotlin
val cache: KottageStorage = kottage.cache("my_item_cache")
val now: Long = ... // Unix Time (UTC) in millis
launch {
    cache.eventFlow(now).collect { event ->
        // receive events from flow
        val eventType: KottageEventType = event.eventType // eventType => KottageEventType.Create
        val updatedValue: String = cache.get<String>(event.itemKey) // updatedValue => "value"
    }
}
cache.put("key", "value")
// get events after time
val events: List<KottageEvent> = cache.getEvents(now)
val updatedValue: String = cache.get<String>(event.first().itemKey) // updatedValue => "value"
```

An eventFlow (KottageEventFlow) can automatically resume from previous emitted event.
For example, on Android platform, collect events while Lifecycle is at least STARTED.

```kotlin
val cache = kottage.cache("my_item_cache")
val now = ... // Unix Time (UTC) in millis
val eventFlow = cache.eventFlow(now)

...

override fun onCreate(...) { // for example: onCreate
    ...
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            eventFlow.collect { event ->
                // eventFlow starts dispatching events from last emitted event on previous subscription.
            }
        }
    }
}
```

# Encryption

User defined encryption are supported. **Only KVS value part is encrypted**, while other part (KVS
key, storage name...) remains plain data.

| part                         | stored data |
|------------------------------|-------------|
| KVS value                    | encrypted   |
| KVS key                      | plain       |
| Kottage name (SQL file name) | plain       |
| Kottage Storage name         | plain       |
| Kottage List name            | plain       |
| KottageListMetaData          | plain       |
| KottageEvent                 | plain       |

```kotlin
val storage: KottageStorage = kottage.storage("encrypted_storage") {
    encoder = object : KottageEncoder {
        override fun encode(value: ByteArray): ByteArray {
            // Your encoding logic (plain ByteArray to encrypted ByteArray) here
            return ...
        }

        override fun decode(encoded: ByteArray): ByteArray {
            // Your decoding logic (encrypted ByteArray to plain ByteArray) here
            return ...
        }
    }
}
// storage's values are encrypted
storage.put("long_value", 100L)
storage.put("string_value", "value")
val longValue: Long = storage.get("long_value") // => 100L
val stringValue: String = storage.get("long_value") // => "value"
```

* Recommendation: [Krypto](https://docs.korge.org/krypto/) is a cool library to use encryption
  feature in Kotlin Multiplatform.

# Supporting Data Types

* Primitives: `Double`, `Float`, `Long`, `Int`, `Short`, `Byte`, `Boolean`
* Bytes: `ByteArray`
* Texts: `String`
* Serializable: kotlinx.serialization's `@Serializable` classes

# Multiplatform

Kottage is a Kotlin Multiplatform library. Please feel free to report a issue if it doesn't
work correctly on these platforms.

| Platform                          | Target                                                                               | Status                                                        |
|-----------------------------------|--------------------------------------------------------------------------------------|---------------------------------------------------------------|
| Kotlin/JVM on Linux/macOS/Windows | jvm                                                                                  | :white_check_mark: Tested                                     |
| Kotlin/JS on Linux/macOS/Windows  | browser, nodejs                                                                      | :white_check_mark: Tested<br/>browser on macOS Chrome, Safari |
| Kotlin/Android                    | android                                                                              | :white_check_mark: Tested                                     |
| Kotlin/Native iOS                 | iosArm64<br>iosX64(simulator)<br>iosSimulatorArm64                                   | :white_check_mark: Tested (by iosSimulatorArm64 only)         |
| Kotlin/Native watchOS             | watchosArm64<br>watchosDeviceArm64<br>watchosX64(simulator)<br>watchosSimulatorArm64 | :white_check_mark: (Tested as iosSimulatorArm64)              |
| Kotlin/Native tvOS                | tvosArm64<br>tvosX64(simulator)<br>tvosSimulatorArm64                                | :white_check_mark: (Tested as iosSimulatorArm64)              |
| Kotlin/Native macOS               | macosArm64<br>macosX64                                                               | :white_check_mark: Tested (by macosArm64 only)                |
| Kotlin/Native Linux               | linuxX64<br>linuxArm64                                                               | :white_check_mark: Tested (by linuxX64 only)                  |
| Kotlin/Native Windows             | mingwX64                                                                             | :white_check_mark: Tested                                     |

There is also [Kottage for SwiftPM](https://github.com/irgaly/kottage-package) that is **just for
experimental** build.

## Kotlin/JS (browser/nodejs) Support

Kottage supports Kottage/JS browser and nodejs. Kottage on browser uses IndexedDB as persistent
database instead of SQLite. Kottage on nodejs uses SQLite.

| Kotlin/JS type | Database  |
|----------------|-----------|
| browser        | IndexedDB |
| nodejs         | SQLite    |

Browsers will clear IndexedDB data when user's disk storage gets low disk space.
You can request browsers your IndexedDB's data not to be cleared by using `StorageManager.persist()`
.
See [Web API documents](https://developer.mozilla.org/en-US/docs/Web/API/StorageManager/persist) for
more details.

### Kotlin/JS browser Setup

Kotlin/JS's library contains both of implementations for browser and for nodejs, so additional
Webpack config is required for browser to use Kottage.

When you run `jsBrowserRun` (jsBrowserDevelopmentRun or jsBrowserProductionRun), some Webpack Errors
occurs:

```shell
Compiled with problems:X

WARNING in ../../node_modules/better-sqlite3/lib/database.js 50:10-81

Critical dependency: the request of a dependency is an expression


ERROR in ../../node_modules/better-sqlite3/lib/database.js 2:11-24

Module not found: Error: Can't resolve 'fs' in '(projectdir)/build/js/node_modules/better-sqlite3/lib'


ERROR in ../../node_modules/better-sqlite3/lib/database.js 3:13-28

Module not found: Error: Can't resolve 'path' in '(projectdir)/build/js/node_modules/better-sqlite3/lib'

BREAKING CHANGE: webpack < 5 used to include polyfills for node.js core modules by default.
This is no longer the case. Verify if you need this module and configure a polyfill for it.

If you want to include a polyfill, you need to:
	- add a fallback 'resolve.fallback: { "path": require.resolve("path-browserify") }'
	- install 'path-browserify'
If you don't want to include a polyfill, you can use an empty module like this:
	resolve.fallback: { "path": false }

...
```

<details>
<summary>all error's text:</summary>

```shell
Compiled with problems:X

WARNING in ../../node_modules/better-sqlite3/lib/database.js 50:10-81

Critical dependency: the request of a dependency is an expression


ERROR in ../../node_modules/better-sqlite3/lib/database.js 2:11-24

Module not found: Error: Can't resolve 'fs' in '(projectdir)/build/js/node_modules/better-sqlite3/lib'


ERROR in ../../node_modules/better-sqlite3/lib/database.js 3:13-28

Module not found: Error: Can't resolve 'path' in '(projectdir)/build/js/node_modules/better-sqlite3/lib'

BREAKING CHANGE: webpack < 5 used to include polyfills for node.js core modules by default.
This is no longer the case. Verify if you need this module and configure a polyfill for it.

If you want to include a polyfill, you need to:
	- add a fallback 'resolve.fallback: { "path": require.resolve("path-browserify") }'
	- install 'path-browserify'
If you don't want to include a polyfill, you can use an empty module like this:
	resolve.fallback: { "path": false }


ERROR in ../../node_modules/better-sqlite3/lib/methods/backup.js 2:11-24

Module not found: Error: Can't resolve 'fs' in '(projectdir)/build/js/node_modules/better-sqlite3/lib/methods'


ERROR in ../../node_modules/better-sqlite3/lib/methods/backup.js 3:13-28

Module not found: Error: Can't resolve 'path' in '(projectdir)/build/js/node_modules/better-sqlite3/lib/methods'

BREAKING CHANGE: webpack < 5 used to include polyfills for node.js core modules by default.
This is no longer the case. Verify if you need this module and configure a polyfill for it.

If you want to include a polyfill, you need to:
	- add a fallback 'resolve.fallback: { "path": require.resolve("path-browserify") }'
	- install 'path-browserify'
If you don't want to include a polyfill, you can use an empty module like this:
	resolve.fallback: { "path": false }


ERROR in ../../node_modules/better-sqlite3/lib/methods/backup.js 4:22-37

Module not found: Error: Can't resolve 'util' in '(projectdir)/build/js/node_modules/better-sqlite3/lib/methods'

BREAKING CHANGE: webpack < 5 used to include polyfills for node.js core modules by default.
This is no longer the case. Verify if you need this module and configure a polyfill for it.

If you want to include a polyfill, you need to:
	- add a fallback 'resolve.fallback: { "util": require.resolve("util/") }'
	- install 'util'
If you don't want to include a polyfill, you can use an empty module like this:
	resolve.fallback: { "util": false }


ERROR in ../../node_modules/bindings/bindings.js 5:9-22

Module not found: Error: Can't resolve 'fs' in '(projectdir)/build/js/node_modules/bindings'


ERROR in ../../node_modules/bindings/bindings.js 6:9-24

Module not found: Error: Can't resolve 'path' in '(projectdir)/build/js/node_modules/bindings'

BREAKING CHANGE: webpack < 5 used to include polyfills for node.js core modules by default.
This is no longer the case. Verify if you need this module and configure a polyfill for it.

If you want to include a polyfill, you need to:
	- add a fallback 'resolve.fallback: { "path": require.resolve("path-browserify") }'
	- install 'path-browserify'
If you don't want to include a polyfill, you can use an empty module like this:
	resolve.fallback: { "path": false }


ERROR in ../../node_modules/file-uri-to-path/index.js 6:10-29

Module not found: Error: Can't resolve 'path' in '(projectdir)/build/js/node_modules/file-uri-to-path'

BREAKING CHANGE: webpack < 5 used to include polyfills for node.js core modules by default.
This is no longer the case. Verify if you need this module and configure a polyfill for it.

If you want to include a polyfill, you need to:
	- add a fallback 'resolve.fallback: { "path": require.resolve("path-browserify") }'
	- install 'path-browserify'
If you don't want to include a polyfill, you can use an empty module like this:
	resolve.fallback: { "path": false }

Compiled with problems:X

ERROR in ./kotlin/kottage-project-core.js 72:11-24

Module not found: Error: Can't resolve 'fs' in '(projectdir)/build/js/packages/kottage-project-js-browser/kotlin'
```

</details>

To suppress this errors, add Webpack config file to project directory. This config will ignore
better-sqlite3 module that is not needed in browser application, and exclude packed files.

`{youar application module path}/webpack.config.d/kottage.webpack.config.js`:

```javascript
config.resolve.fallback = {
    ...config.resolve.fallback,
    fs: false,
    os: false
}
config.externals = {
    ...config.externals,
    "better-sqlite3": "better-sqlite3"
}
```

Sample application project is available in [sample/js-browser](sample/js-browser).

### Kotlin/JS nodejs Setup

Kottage uses [better-sqlite3](https://github.com/WiseLibs/better-sqlite3) on nodejs.

better-sqlite3 requires sqlite3 FFI file `better_sqlite3.node` on runtime environment.
If there is no FFI file, `Could not locate the bindings file` error occurred:

```shell
CoroutinesInternalError: Fatal exception in coroutines machinery for AwaitContinuation(DispatchedContinuation[NodeDispatcher@1, [object Object]]){Completed}@2. Please read KDoc to 'handleFatalException' method and report this incident to maintainers
    at AwaitContinuation.DispatchedTask.handleFatalException_56zdfo_k$ ((projectdir)/DispatchedTask.kt:144:22)
    at AwaitContinuation.DispatchedTask.run_mw4iiu_k$ ((projectdir)/DispatchedTask.kt:115:13)
    at ScheduledMessageQueue.MessageQueue.process_mza50i_k$ ((projectdir)/JSDispatcher.kt:153:25)
    at (projectdir)/JSDispatcher.kt:19:48
    at processTicksAndRejections (node:internal/process/task_queues:77:11) {
  cause: Error: Could not locate the bindings file. Tried:
   → (projectdir)/build/js/node_modules/better-sqlite3/build/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/build/Debug/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/build/Release/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/out/Debug/better_sqlite3.node

...
```

<details>
<summary>all error's text:</summary>

```shell
CoroutinesInternalError: Fatal exception in coroutines machinery for AwaitContinuation(DispatchedContinuation[NodeDispatcher@1, [object Object]]){Completed}@2. Please read KDoc to 'handleFatalException' method and report this incident to maintainers
    at AwaitContinuation.DispatchedTask.handleFatalException_56zdfo_k$ ((projectdir)/DispatchedTask.kt:144:22)
    at AwaitContinuation.DispatchedTask.run_mw4iiu_k$ ((projectdir)/DispatchedTask.kt:115:13)
    at ScheduledMessageQueue.MessageQueue.process_mza50i_k$ ((projectdir)/JSDispatcher.kt:153:25)
    at (projectdir)/JSDispatcher.kt:19:48
    at processTicksAndRejections (node:internal/process/task_queues:77:11) {
  cause: Error: Could not locate the bindings file. Tried:
   → (projectdir)/build/js/node_modules/better-sqlite3/build/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/build/Debug/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/build/Release/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/out/Debug/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/Debug/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/out/Release/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/Release/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/build/default/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/compiled/18.11.0/darwin/arm64/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/addon-build/release/install-root/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/addon-build/debug/install-root/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/addon-build/default/install-root/better_sqlite3.node
   → (projectdir)/build/js/node_modules/better-sqlite3/lib/binding/node-v108-darwin-arm64/better_sqlite3.node
      at bindings ((projectdir)/build/js/node_modules/bindings/bindings.js:126:9)
      at new Database ((projectdir)/build/js/node_modules/better-sqlite3/lib/database.js:48:64)
      at Database ((projectdir)/build/js/node_modules/better-sqlite3/lib/database.js:11:10)
      at $createDriverCOROUTINE$0.doResume_5yljmg_k$ ((projectdir)/DriverFactory.kt:35:13)
      at DriverFactory.createDriver_qrqvgc_k$ ((projectdir)/DriverFactory.kt:15:20)
      at createDriver ((projectdir)/DriverFactory.kt:32:12)
      at createDriver$default ((projectdir)/DriverFactory.kt:27:9)
      at SqliteDatabaseConnectionFactory$createDatabaseConnection$slambda.doResume_5yljmg_k$ ((projectdir)/SqliteDatabaseConnectionFactory.kt:28:15)
      at SqliteDatabaseConnectionFactory$createDatabaseConnection$slambda.invoke_uw69q_k$ ((projectdir)/SqliteDatabaseConnectionFactory.kt:21:41)
      at SqliteDatabaseConnection.l [as sqlDriverProvider_1] ((projectdir)/build/js/packages/kottage-project-js-nodejs/kotlin/kottage-project-kottage.js:28289:16) {
    tries: [
      '(projectdir)/build/js/node_modules/better-sqlite3/build/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/build/Debug/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/build/Release/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/out/Debug/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/Debug/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/out/Release/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/Release/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/build/default/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/compiled/18.11.0/darwin/arm64/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/addon-build/release/install-root/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/addon-build/debug/install-root/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/addon-build/default/install-root/better_sqlite3.node',
      '(projectdir)/build/js/node_modules/better-sqlite3/lib/binding/node-v108-darwin-arm64/better_sqlite3.node'
    ]
  }
}
```

</details>

To prevent this error, you should build FFI file with a custom Gradle Task.

* Make sure your machine environment has `python3`.
* Register `installBetterSqlite3` task. This task will
  make `{rootProject}/build/js/node_modules/better-sqlite3/build/Release/better_sqlite3.node`.

`{rootProject}/build.gradle.kts` ([sample build.gradle.kts is here](build.gradle.kts))

```kotlin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

...
plugins.withType<NodeJsRootPlugin> {
    extensions.configure<NodeJsRootExtension> {
        // Choose any version you want to use from https://nodejs.org/en/download/releases/
        nodeVersion = "19.9.0"
        val installBetterSqlite3 by tasks.registering(Exec::class) {
            val nodeExtension = this@configure
            val nodeEnv = nodeExtension.requireConfigured()
            val node = nodeEnv.nodeExecutable.replace(File.separator, "/")
            val nodeDir = nodeEnv.nodeDir.path.replace(File.separator, "/")
            val nodeBinDir = nodeEnv.nodeBinDir.path.replace(File.separator, "/")
            val npmCli = if (OperatingSystem.current().isWindows) {
                "$nodeDir/node_modules/npm/bin/npm-cli.js"
            } else {
                "$nodeDir/lib/node_modules/npm/bin/npm-cli.js"
            }
            val npm = "\"$node\" \"$npmCli\""
            val betterSqlite3 = buildDir.resolve("js/node_modules/better-sqlite3")
            dependsOn(tasks.withType<KotlinNpmInstallTask>())
            inputs.files(betterSqlite3.resolve("package.json"))
            inputs.property("node-version", nodeVersion)
            outputs.files(betterSqlite3.resolve("build/Release/better_sqlite3.node"))
            outputs.cacheIf { true }
            workingDir = betterSqlite3
            commandLine = if (OperatingSystem.current().isWindows) {
                listOf(
                    "sh",
                    "-c",
                    // use pwd command to convert C:/... -> /c/...
                    "PATH=\$(cd $nodeBinDir;pwd):\$PATH $npm run install --verbose"
                )
            } else {
                listOf(
                    "sh",
                    "-c",
                    "PATH=\"$nodeBinDir:\$PATH\" $npm run install --verbose"
                )
            }
        }
    }
}
...
```

* Add NodeJsExec Task's dependency
  setting. ([sample sample/js-nodejs/build.gradle.kts is here](sample/js-nodejs/build.gradle.kts))

`{nodejs project}/build.gradle.kts`

```kotlin
plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        ...
    }
    ...
}
...
tasks.withType<NodeJsExec>().configureEach {
    dependsOn(rootProject.tasks.named("installBetterSqlite3"))
}
...
```

* Then, execute `jsNodeRun` (jsNodeDevelopmentRun or jsNodeProductionRun) task. There are no FFI
  errors.

# Kottage Internals

TBA: I'll write details of library here.

* Lazy item expiration.
* Automated clean up of expired item.
* Limit item counts.

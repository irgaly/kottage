# Kottage

Kotlin Multiplatform Key-Value Store Local Cache Storage for Single Source of Truth.

# Features

* A Kotlin Multiplatform library
* Key-Value Store with no schemas, that values are stored based on SQLite
* Observing events of item updates as Flow
* Cache Expiration
    * Cache Eviction Strategy Options:
        * Expiration Time
        * FIFO Strategy
        * LRU Strategy
* KVS Cache mode / KVS Storage mode
    * Expired items are evicted automatically
    * There is a storage mode with no item expiration
* Support primitive values and `@Serializable` classes

# Requires

* [New memory manager](https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md)
  enabled with Kotlin/Native platform.

# Usage

## Setup

Add Kottage as gradle dependency.

`build.gradle.kts`

```kotlin
// For Kotlin Multiplatform
kotlin {
    sourceSets {
        commonMain {
            implementation("io.github.irgaly.kottage:kottage:1.0.0")
        }
    }
}

// It can be used as a JVM library for Kotlin/JVM or Kotlin/Android
dependencies {
    implementation("io.github.irgaly.kottage:kottage:1.0.0")
}
```

## Enable Kotlin/Native New Memory Manager

Enable Kotlin/Native New Memory Manger in gradle.properties if your project using before Kotlin
1.7.20.

`gradle.properties`

```properties
# memoryModel experimental is enabled by default from Kotlin 1.7.20
kotlin.native.binary.memoryModel=experimental
```

## Use Kottage

Use Kottage as KVS cache or KVS storage.

At first, get a Kottage instance. Even though you can use Kottage instance as a singleton, multiple
Kottage instances creation is allowed. Kottage instances and methods are thread safe.

```kotlin
import io.github.irgaly.kottage.platform.contextOf

// directory path string for SQLite file
// For example:
// * Android File Directory: context.getFilesDir().path
// * Android Cache Directory: context.getCacheDir().path
val databaseDirectory: String = ...
val kottageEnvironment: KottageEnvironment = KottageEnvironment(
    context = contextOf(context), // for Android, set a KottageContext with Android Context object
    //context = KottageContext(), // for other platforms, set an empty KottageContext
    calendar = object : KottageCalendar {
        override fun nowUnixTimeMillis(): Long {
            // for example: JVM / Android Unix Time implementation
            return System.currentTimeMillis
        }
    }
)
// Initialize with Kottage database information.
val kottage: Kottage = Kottage(
    name = "kottage-store-name", // This will be database file name
    directoryPath = databaseDirectory,
    environment = kottageEnvironment,
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
// These items will be expired and automatically deleted after 30 days elapsed
cache.put("item1", "item1 value")
cache.put("item2", 42)
cache.put("item3", true)
val value1: String = cache.get<String>("item1")
val value2: Int = cache.get<Int>("item2")
val value3: Boolean = cache.get<Boolean>("item3")
cache.exists("item4") // => false
cache.getOrNull<String>("item4") // => null
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
cache.eventFlow(now).collect { event ->
    // receive events from flow
    val eventType: KottageEventType = event.eventType // eventType => KottageEventType.Create
    val updatedValue: String = cache.get<String>(event.itemKey) // updatedValue => "value"
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

# Supporting Data Types

* Primitives: `Double`, `Float`, `Long`, `Int`, `Short`, `Byte`, `Boolean`
* Bytes: `ByteArray`
* Texts: `String`
* Serializable: kotlinx.serialization's `@Serializable` classes

# Multiplatform

Kottage is a Kotlin Multiplatform library. Please feel free to report a issue if it doesn't
work correctly on these platforms.

| Platform              | Target                                                         | Status                                                                        |
|-----------------------|----------------------------------------------------------------|-------------------------------------------------------------------------------|
| Kotlin/JVM            | jvm                                                            | :white_check_mark: Supported, :white_check_mark: Tested                       |
| Kotlin/JS             | js                                                             | :x: Not supported, support in future release.                                 |
| Kotlin/Android        | android                                                        | :white_check_mark: Supported, :tired_face: currently no automated unit tests. |
| Kotlin/Native iOS     | iosArm64<br>iosX64(simulator)<br>iosSimulatorArm64             | :white_check_mark: Supported, :+1: Tested as Darwin on macOS                  |
| Kotlin/Native watchOS | watchosArm64<br>watchosX64(simulator)<br>watchosSimulatorArm64 | :white_check_mark: Supported, :+1: Tested as Darwin on macOS                  |
| Kotlin/Native tvOS    | tvosArm64<br>tvosX64(simulator)<br>tvosSimulatorArm64          | :white_check_mark: Supported, :+1: Tested as Darwin on macOS                  |
| Kotlin/Native macOS   | macosArm64<br>macosX64                                         | :white_check_mark: Supported, :white_check_mark: Tested                       |
| Kotlin/Native Linux   | linuxX64                                                       | :white_check_mark: Supported, :tired_face: currently no automated unit tests. |
| Kotlin/Native Windows | mingwX64                                                       | :white_check_mark: Supported, :tired_face: currently no automated unit tests. |

# Kottage Internals

TBA: I'll write details of library here.

* Lazy item expiration.
* Automated clean up of expired item.
* Limit item counts.

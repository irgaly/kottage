# Kottage

Kotlin Multiplatform Key-Value Store Local Cache Storage for Single Source of Truth.

# Features

* SQLite based Key-Value Store
* Observing events as Flow when items are changed
* Cache Expiration
    * Cache Eviction Strategy Options:
        * Expiration Time
        * FIFO Strategy
        * LRU Strategy
* KVS Storage mode
    * Simple KVS Store with no item eviction
* Kotlin Multiplatform
* Support primitive values and `@Serializable` classes

# Requires

* [New memory manager](https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md)
  enabled with Kotlin/Native platform.

# Usage

## Setup

Add kottage as gradle dependency.

`build.gradle.kts`

```kotlin
// For Kotlin Multiplatform
kotlin {
    sourceSets {
        commonMain {
            implementation("io.github.irgaly.kottage:kottage:0.9.1")
        }
    }
}

// For Kotlin/JVM, Kotlin/Android
dependencies {
    implementation("io.github.irgaly.kottage:kottage:0.9.1")
}
```

## Enable Kotlin/Native New Memory Manager

Enable Kotlin/Native New Memory Manger in gradle.properties if your project using before Kotlin
1.7.20.

`gradle.properties`

```properties
# memoryModel experimental is default from Kotlin 1.7.20
kotlin.native.binary.memoryModel=experimental
```

## Use

Use kottage as KVS cache or KVS storage.

```kotlin
...
```

### Event Observing

```kotlin
...
```

# Supporting Data Types

* Primitives: Double, Float, Long, Int, Short, Byte, Boolean
* Bytes: ByteArray
* Texts: String
* Serializable: kotlinx.serialization's `@Serializable` classes

# Multiplatform

Kottage is a Kotlin Multiplatform library. Please feel free to report a issue if kottage doesn't
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

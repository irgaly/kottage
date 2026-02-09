# v1.11.0 - 2026/2/9 JST

#### Feature

* add KottageStorage.getOrPut() [#362](https://github.com/irgaly/kottage/pull/362)

#### Fix

* Fix jsBrowser (indexeddb) KottageStorage.removeAll() throws
  IllegalStateException#361 [#361](https://github.com/irgaly/kottage/pull/361)

# v1.10.0 - 2025/12/9 JST

#### Feature

* KottageFifoStrategy, KottageLruStrategy supports
  maxCacheSize [#346](https://github.com/irgaly/kottage/pull/346)

#### Maintenance

* fix(deps): update sqldelight to v2.2.1 [#343](https://github.com/irgaly/kottage/pull/343)

# v1.9.0 - 2025/10/14 JST

#### Maintenance

* update: kotest v6.0.3, Kotlin v2.2.0, Android minSdk = 26, Node 22.0.0, better-sqlite3
  v12.4.1 [#330](https://github.com/irgaly/kottage/pull/330)
* Update SQLDelight v2.1.0 [#312](https://github.com/irgaly/kottage/pull/312)

# v1.8.0 - 2024/06/09 JST

#### Improvement

* Support watchosDeviceArm64, update SQLDelight
  2.0.1 [#170](https://github.com/irgaly/kottage/pull/170)
* Support linuxArm64 target [#233](https://github.com/irgaly/kottage/pull/233)

#### Maintenance

* CI: Run Arm64 Test on macOS [#234](https://github.com/irgaly/kottage/pull/234)

# v1.7.0 - 2024/06/03 JST

#### Maintenance

* Kotlin 2.0.0
    * Update Kotlin 2.0.0, Gradle 8.8, Kotest 5.9.0, jvmToolchain(17) [#230](https://github.com/irgaly/kottage/pull/230)
* better-sqlite3 9.2.2
    * Update better-sqlite 9.2.2, fix python3 3.12 ModuleNotFoundError [#204](https://github.com/irgaly/kottage/pull/204)
* AGP 8.3.1
* indexeddb 0.7.1
    * fix(deps): update dependency com.juul.indexeddb:core to v0.7.1 [#218](https://github.com/irgaly/kottage/pull/218)

# v1.6.0 - 2023/09/06 JST

This is a release for updating libraries:

* Kotlin 1.9.10
* SQLDelight 2.0.0
* Gradle 8.3
* AGP 8.3

#### Changes

* Android minSdk = 21 (from minSdk = 16)
  * [#127](https://github.com/irgaly/kottage/pull/127)
* Migrate SQLDelight 2.0.0 [179](https://github.com/irgaly/kottage/pull/179)
* Android Platform: SQLite lock file to
  ${name}.db.lck [182](https://github.com/irgaly/kottage/pull/182)

#### Fix

* fix Linux Event Test, fix KottageEventFlow [#148](https://github.com/irgaly/kottage/pull/148)

#### Maintenance

* Add CI Test:
  * Android, iOS Test, Windows Test [#127](https://github.com/irgaly/kottage/pull/127)
  * Add JS Test on Windows, Linux [#152](https://github.com/irgaly/kottage/pull/152)
* Refactor Gradle Scripts
  * publish-plugin 1.2.0, use
    providers.environmentVariable [#140](https://github.com/irgaly/kottage/pull/140)
  * refactor Gradle, Kotlin 1.8.10, SQLDelight
    1.5.5 [#141](https://github.com/irgaly/kottage/pull/141)
* Update Kotlin, use New hierarchy SourceSets
  * Update Kotlin 1.8.20 + Use New Default
    hierarchy [#169](https://github.com/irgaly/kottage/pull/169)
  * Kotlin 1.9.0 + Compose Compiler 1.5.0 [#166](https://github.com/irgaly/kottage/pull/166)
  * Kotlin v1.9.10, Compose Compiler 1.5.3 [#184](https://github.com/irgaly/kottage/pull/184)
* Update Gradle 8.3
  * Update Gradle 8 [#158](https://github.com/irgaly/kottage/pull/158)
  * chore(deps): update dependency gradle to v8.3 [#171](https://github.com/irgaly/kottage/pull/171)
* Update AGP 8.3
  * chore(deps): update gradle.android to v8.1.1 [#156](https://github.com/irgaly/kottage/pull/156)

# v1.5.1 - 2023/04/03 JST

#### Fix

* fix: KottageContext constructor modifier [#138](https://github.com/irgaly/kottage/pull/138)

# v1.5.0 - 2023/02/12 JST

#### Changes

* Kottage constructor requires CoroutineScope now.

```kotlin
val kottage: Kottage = Kottage(
  name = "kottage-name",
  directoryPath = databaseDirectory,
  environment = kottageEnvironment,
  scope = scope // This kottage instance's living CoroutineScope
)
```

* change: Database schema to v4
  * add index
    item_event_item_list_type_item_type_created_at [#120](https://github.com/irgaly/kottage/pull/120)

#### Improve

* Support Kottage.close() [#114](https://github.com/irgaly/kottage/pull/114)
* add KottageListPage.isNotEmpty() [#112](https://github.com/irgaly/kottage/pull/112)
* Add list event on item update [#117](https://github.com/irgaly/kottage/pull/117)
* Add KottageList.eventFlow(), exclude List Event from
  KottageStorage.eventFlow() [#120](https://github.com/irgaly/kottage/pull/120)
* Add KottageStorage.dropStorage(), KottageList.dropList(),
  KottageList.removeAll() [#122](https://github.com/irgaly/kottage/pull/122)

#### Fix

* KottageList.getPageFrom hangups with invalid
  positionId [#110](https://github.com/irgaly/kottage/pull/110)
* fix PRAGMA query leak on Android [#115](https://github.com/irgaly/kottage/pull/115)
* fix: getPageFrom previousPositionId, nextPositionId
  nullability [#123](https://github.com/irgaly/kottage/pull/123)

# v1.4.2 - 2022/11/22 JST

#### Maintenance

* update Kotlin/JS nodejs's better-sqlite3 8.0.0, SQLite3
  3.40.0 [#86](https://github.com/irgaly/kottage/pull/86)

# v1.4.1 - 2022/11/15 JST

#### Improve

* add default KottageCalendar implementation [#94](https://github.com/irgaly/kottage/pull/94)

# v1.4.0 - 2022/11/08 JST

#### Features

* :tada: Kotlin/JS nodejs support [#79](https://github.com/irgaly/kottage/pull/79)

#### Fix

* implement Kottage.clear() on JS browser [#83](https://github.com/irgaly/kottage/pull/83)

# v1.3.0 - 2022/10/31 JST

#### Features

* :tada: Kotlin/JS browser support [#58](https://github.com/irgaly/kottage/pull/58)

#### Changes

* change: Database schema to v3
  * add item_expire_at index [#59](https://github.com/irgaly/kottage/pull/59)
  * fix item_list_type_item_type_expire_at to
    item_list_type_item_key_expire_at [#62](https://github.com/irgaly/kottage/pull/61)
  * remove unused index [#63](https://github.com/irgaly/kottage/pull/63)

#### Fix

* fix model class's internal modifier [#71](https://github.com/irgaly/kottage/pull/71)
* fix Event can not be received when that has same create_at
  time [72](https://github.com/irgaly/kottage/pull/72)

# v1.2.0 - 2022/10/22 JST

#### Changes

* change: KottagePage.hasPrevious, hasNext are false if next page has no available items
  * now flags are false in case of nextPositionId != null but no available items.

# v1.1.1 - 2022/10/16 JST

#### Fix

* fix kotlinx.coroutines and kotlinx.serialization to api() dependency

# v1.1.0 - 2022/10/14 JST

#### Features

* implement: KottageStorage.property() property
  delegate. [#36](https://github.com/irgaly/kottage/pull/36)
* add KottageEncoder [#40](https://github.com/irgaly/kottage/pull/40)
  * This supports user's custom encoder, such as encryption.
* add ignoreJsonDeserializationError option [#42](https://github.com/irgaly/kottage/pull/42)

#### Improvements

* fix: prevent auto compaction running twice [#35](https://github.com/irgaly/kottage/pull/35)
* add KottageEntry properties [#36](https://github.com/irgaly/kottage/pull/36)
* delete item_stats record when it's empty [#41](https://github.com/irgaly/kottage/pull/41)

# v1.0.1 - 2022/10/12 JST

#### Fix

* fix Database schema v2 migration
  * fix: invalid column order if database have been migrated from v1 schema.

# v1.0.0 - 2022/10/12 JST

#### Features

* A KottageList feature added.

#### Fix

* fix: KottageStorage.eventFlow() contains other Storages events.

#### Changes

* Database schema v2
  * Database will be migrated automatically on the connection open.
  * item_event.item_key format changed. `{item_type}+{item_key}` to `{item_key}`. This affect the
    events created by kottage before v1.0.0.

# v0.9.1 - 2022/09/23 JST

#### Others

* fix Maven metadata: project url: https://github.com/irgaly/kottage

# v0.9.0 - 2022/09/18 JST

* initial release

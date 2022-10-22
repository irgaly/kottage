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

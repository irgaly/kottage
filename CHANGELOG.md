# v1.0.0 - 2022/10/12

#### Features

* A KottageList feature added.

#### Fix

* fix: KottageStorage.eventFlow() contains other Storages events.

#### Changes

* Database schema v2
    * Database will be migrated automatically on the connection open.
    * item_event.item_key format changed. `{item_type}+{item_key}` to `{item_key}`. This affect the
      events created by kottage before v1.0.0.

# v0.9.1 - 2022/09/23

#### Others

* fix Maven metadata: project url: https://github.com/irgaly/kottage

# v0.9.0 - 2022/09/18

* initial release

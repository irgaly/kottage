package io.github.irgaly.kottage.data.sqlite.extension

import com.squareup.sqldelight.db.SqlDriver


actual fun SqlDriver.executeWalCheckpointTruncate() {
    execute(null, "PRAGMA wal_checkpoint(TRUNCATE)", 0)
}
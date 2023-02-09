package io.github.irgaly.kottage.data.sqlite

import com.squareup.sqldelight.db.SqlDriver
import io.github.irgaly.kottage.platform.Context
import kotlinx.coroutines.CoroutineDispatcher

expect class DriverFactory(
    context: Context,
    dispatcher: CoroutineDispatcher
) {
    /**
     * * Platforms
     *   * JVM: {directory}/{fileName}.db
     *   * Android: {directory}/{fileName}.db
     *   * Native (iOS, macOS, Linux, Windows): {directory}/{fileName}.db
     *   * NodeJS: {directory}/{fileName}.db
     *
     * @param fileName 拡張子を除いた sqlite ファイル名。"{fileName}.db" として保存されます
     * @param directoryPath sqlite を保存するディレクトリ。該当ディレクトリは存在している必要がある。
     */
    suspend fun createDriver(
        fileName: String,
        directoryPath: String,
        schema: SqlDriver.Schema
    ): SqlDriver
}

suspend fun DriverFactory.createDriver(
    fileName: String,
    directoryPath: String,
    version: Int? = null
): SqlDriver {
    return createDriver(
        fileName = fileName,
        directoryPath = directoryPath,
        schema = when (version) {
            1 -> KottageDatabase1.Schema
            2 -> KottageDatabase2.Schema
            3, null -> KottageDatabase.Schema
            else -> throw IllegalArgumentException("unknown schema version: $version")
        }
    )
}

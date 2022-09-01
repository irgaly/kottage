package io.github.irgaly.kottage.data.sqlite

import com.squareup.sqldelight.db.SqlDriver
import io.github.irgaly.kottage.platform.Context
import kotlinx.coroutines.*

expect class DriverFactory(
    context: Context,
    dispatcher: CoroutineDispatcher
) {
    /**
     * * Platforms
     *   * JVM: {directory}/{fileName}.db
     *   * Android: {directory}/{fileName}.db
     *   * Native (iOS, macOS, Linux, Windows): {directory}/{fileName}.db
     *
     * @param fileName 拡張子を除いた sqlite ファイル名。"{fileName}.db" として保存されます
     * @param directoryPath sqlite を保存するディレクトリ。該当ディレクトリは存在している必要がある。
     */
    suspend fun createDriver(fileName: String, directoryPath: String): SqlDriver
}

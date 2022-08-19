package io.github.irgaly.kkvs.data.sqlite

import com.squareup.sqldelight.db.SqlDriver
import io.github.irgaly.kkvs.platform.Context

expect class DriverFactory constructor(context: Context) {
    /**
     * * Platforms
     *   * JVM: {directory}/{fileName}.db
     *   * Android: {directory}/{fileName}.db
     *   * Native (iOS, macOS, Linux, Windows): {directory}/{fileName}.db
     *
     * @param fileName 拡張子を除いた sqlite ファイル名。"{fileName}.db" として保存されます
     * @param directoryPath sqlite を保存するディレクトリ。該当ディレクトリは存在している必要がある。
     */
    fun createDriver(fileName: String, directoryPath: String): SqlDriver
}

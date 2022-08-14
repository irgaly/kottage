package net.irgaly.kkvs.internal

import com.squareup.sqldelight.db.SqlDriver
import net.irgaly.kkvs.KkvsEnvironment

internal expect class DriverFactory constructor(environment: KkvsEnvironment) {
    /**
     * * Platforms
     *   * JVM: {directory}/{fileName}.db
     *   * Android: {directory}/{fileName}.db
     *   * Native (iOS, macOS, Linux, Windows): {directory}/{fileName}.db
     *   * JS: sql.js on memory SQLite
     *     * https://github.com/sql-js/sql.js/
     *     * https://emscripten.org/docs/porting/files/file_systems_overview.html
     *
     * @param fileName 拡張子を除いた sqlite ファイル名。"{fileName}.db" として保存されます
     * @param directoryPath sqlite を保存するディレクトリ。該当ディレクトリは存在している必要がある。
     */
    fun createDriver(fileName: String, directoryPath: String): SqlDriver
}

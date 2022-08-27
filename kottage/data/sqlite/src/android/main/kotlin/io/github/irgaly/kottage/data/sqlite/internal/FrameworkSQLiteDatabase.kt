/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.irgaly.kottage.data.sqlite.internal

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.text.TextUtils
import android.util.Pair
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import java.io.IOException
import java.util.*

/**
 * Delegates all calls to an implementation of [SQLiteDatabase].
 *
 * @param mDelegate The delegate to receive all calls.
 */
internal class FrameworkSQLiteDatabase(private val mDelegate: SQLiteDatabase) :
    SupportSQLiteDatabase {
    override fun compileStatement(sql: String): SupportSQLiteStatement {
        return FrameworkSQLiteStatement(mDelegate.compileStatement(sql))
    }

    override fun beginTransaction() {
        mDelegate.beginTransaction()
    }

    override fun beginTransactionNonExclusive() {
        mDelegate.beginTransactionNonExclusive()
    }

    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) {
        mDelegate.beginTransactionWithListener(transactionListener)
    }

    override fun beginTransactionWithListenerNonExclusive(
        transactionListener: SQLiteTransactionListener
    ) {
        mDelegate.beginTransactionWithListenerNonExclusive(transactionListener)
    }

    override fun endTransaction() {
        mDelegate.endTransaction()
    }

    override fun setTransactionSuccessful() {
        mDelegate.setTransactionSuccessful()
    }

    override fun inTransaction(): Boolean {
        return mDelegate.inTransaction()
    }

    override fun isDbLockedByCurrentThread(): Boolean {
        return mDelegate.isDbLockedByCurrentThread
    }

    override fun yieldIfContendedSafely(): Boolean {
        return mDelegate.yieldIfContendedSafely()
    }

    override fun yieldIfContendedSafely(sleepAfterYieldDelay: Long): Boolean {
        return mDelegate.yieldIfContendedSafely(sleepAfterYieldDelay)
    }

    override fun getVersion(): Int {
        return mDelegate.version
    }

    override fun setVersion(version: Int) {
        mDelegate.version = version
    }

    override fun getMaximumSize(): Long {
        return mDelegate.maximumSize
    }

    override fun setMaximumSize(numBytes: Long): Long {
        return mDelegate.setMaximumSize(numBytes)
    }

    override fun getPageSize(): Long {
        return mDelegate.pageSize
    }

    override fun setPageSize(numBytes: Long) {
        mDelegate.pageSize = numBytes
    }

    override fun query(query: String): Cursor {
        return query(SimpleSQLiteQuery(query))
    }

    override fun query(query: String, bindArgs: Array<Any>): Cursor {
        return query(SimpleSQLiteQuery(query, bindArgs))
    }

    override fun query(supportQuery: SupportSQLiteQuery): Cursor {
        return mDelegate.rawQueryWithFactory({ _, masterQuery, editTable, query ->
            supportQuery.bindTo(FrameworkSQLiteProgram(query))
            SQLiteCursor(masterQuery, editTable, query)
        }, supportQuery.sql, EMPTY_STRING_ARRAY, null)
    }

    override fun query(
        supportQuery: SupportSQLiteQuery,
        cancellationSignal: CancellationSignal
    ): Cursor {
        return mDelegate.rawQueryWithFactory(
            { _, masterQuery, editTable, query ->
                supportQuery.bindTo(FrameworkSQLiteProgram(query))
                SQLiteCursor(masterQuery, editTable, query)
            },
            supportQuery.sql,
            EMPTY_STRING_ARRAY,
            null,
            cancellationSignal
        )
    }

    @Throws(SQLException::class)
    override fun insert(table: String, conflictAlgorithm: Int, values: ContentValues): Long {
        return mDelegate.insertWithOnConflict(
            table, null, values,
            conflictAlgorithm
        )
    }

    override fun delete(table: String, whereClause: String, whereArgs: Array<Any>): Int {
        val query = ("DELETE FROM " + table
                + if (TextUtils.isEmpty(whereClause)) "" else " WHERE $whereClause")
        val statement = compileStatement(query)
        SimpleSQLiteQuery.bind(statement, whereArgs)
        return statement.executeUpdateDelete()
    }

    override fun update(
        table: String, conflictAlgorithm: Int, values: ContentValues, whereClause: String,
        whereArgs: Array<Any>
    ): Int {
        // taken from SQLiteDatabase class.
        require(0 < values.size()) { "Empty values" }
        val sql = java.lang.StringBuilder(120)
        sql.append("UPDATE ")
        sql.append(CONFLICT_VALUES[conflictAlgorithm])
        sql.append(table)
        sql.append(" SET ")

        // move all bind args to one array
        val setValuesSize = values.size()
        val bindArgsSize = setValuesSize + whereArgs.size
        val bindArgs = arrayOfNulls<Any>(bindArgsSize)
        var i = 0
        for (colName in values.keySet()) {
            sql.append(if (i > 0) "," else "")
            sql.append(colName)
            bindArgs[i++] = values[colName]
            sql.append("=?")
        }
        i = setValuesSize
        while (i < bindArgsSize) {
            bindArgs[i] = whereArgs[i - setValuesSize]
            i++
        }
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ")
            sql.append(whereClause)
        }
        val stmt = compileStatement(sql.toString())
        SimpleSQLiteQuery.bind(stmt, bindArgs)
        return stmt.executeUpdateDelete()
    }

    @Throws(SQLException::class)
    override fun execSQL(sql: String) {
        mDelegate.execSQL(sql)
    }

    @Throws(SQLException::class)
    override fun execSQL(sql: String, bindArgs: Array<Any>) {
        mDelegate.execSQL(sql, bindArgs)
    }

    override fun isReadOnly(): Boolean {
        return mDelegate.isReadOnly
    }

    override fun isOpen(): Boolean {
        return mDelegate.isOpen
    }

    override fun needUpgrade(newVersion: Int): Boolean {
        return mDelegate.needUpgrade(newVersion)
    }

    override fun getPath(): String {
        return mDelegate.path
    }

    override fun setLocale(locale: Locale) {
        mDelegate.setLocale(locale)
    }

    override fun setMaxSqlCacheSize(cacheSize: Int) {
        mDelegate.setMaxSqlCacheSize(cacheSize)
    }

    override fun setForeignKeyConstraintsEnabled(enable: Boolean) {
        mDelegate.setForeignKeyConstraintsEnabled(enable)
    }

    override fun enableWriteAheadLogging(): Boolean {
        return mDelegate.enableWriteAheadLogging()
    }

    override fun disableWriteAheadLogging() {
        mDelegate.disableWriteAheadLogging()
    }

    override fun isWriteAheadLoggingEnabled(): Boolean {
        return mDelegate.isWriteAheadLoggingEnabled
    }

    override fun getAttachedDbs(): List<Pair<String, String>> {
        return mDelegate.attachedDbs
    }

    override fun isDatabaseIntegrityOk(): Boolean {
        return mDelegate.isDatabaseIntegrityOk
    }

    @Throws(IOException::class)
    override fun close() {
        mDelegate.close()
    }

    /**
     * Checks if this object delegates to the same given database reference.
     */
    fun isDelegate(sqLiteDatabase: SQLiteDatabase): Boolean {
        return mDelegate == sqLiteDatabase
    }

    companion object {
        private val CONFLICT_VALUES =
            arrayOf("", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE ")
        private val EMPTY_STRING_ARRAY = arrayOfNulls<String>(0)
    }
}

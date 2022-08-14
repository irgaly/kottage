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
package net.irgaly.kkvs.data.sqlite.internal

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import java.io.File

internal class FrameworkSQLiteOpenHelper @JvmOverloads constructor(
    private val mContext: Context,
    private val mName: String?,
    private val mCallback: SupportSQLiteOpenHelper.Callback,
    private val directoryPath: String? = null,
    private val mUseNoBackupDirectory: Boolean = false
) : SupportSQLiteOpenHelper {
    private val mLock: Any = Any()

    // Delegate is created lazily
    private var mDelegate: OpenHelper? = null
    private var mWriteAheadLoggingEnabled = false

    // getDelegate() is lazy because we don't want to File I/O until the call to
    // getReadableDatabase() or getWritableDatabase(). This is better because the call to
    // a getReadableDatabase() or a getWritableDatabase() happens on a background thread unless
    // queries are allowed on the main thread.

    // We defer computing the path the database from the constructor to getDelegate()
    // because context.getNoBackupFilesDir() does File I/O :(
    private val delegate: OpenHelper
        get() {
            // getDelegate() is lazy because we don't want to File I/O until the call to
            // getReadableDatabase() or getWritableDatabase(). This is better because the call to
            // a getReadableDatabase() or a getWritableDatabase() happens on a background thread unless
            // queries are allowed on the main thread.

            // We defer computing the path the database from the constructor to getDelegate()
            // because context.getNoBackupFilesDir() does File I/O :(
            synchronized(mLock) {
                if (mDelegate == null) {
                    val dbRef = arrayOfNulls<FrameworkSQLiteDatabase>(1)
                    mDelegate =
                        if (directoryPath != null && mName != null) {
                            val file = File(directoryPath, mName)
                            OpenHelper(mContext, file.absolutePath, dbRef, mCallback)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mName != null && mUseNoBackupDirectory) {
                            val file = File(
                                mContext.noBackupFilesDir,
                                mName
                            )
                            OpenHelper(mContext, file.absolutePath, dbRef, mCallback)
                        } else {
                            OpenHelper(mContext, mName, dbRef, mCallback)
                        }.apply {
                            setWriteAheadLoggingEnabled(mWriteAheadLoggingEnabled)
                        }
                }
                return mDelegate!!
            }
        }

    override fun getDatabaseName(): String? {
        return mName
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        synchronized(mLock) {
            mDelegate?.setWriteAheadLoggingEnabled(enabled)
            mWriteAheadLoggingEnabled = enabled
        }
    }

    override fun getWritableDatabase(): SupportSQLiteDatabase {
        return delegate.writableSupportDatabase
    }

    override fun getReadableDatabase(): SupportSQLiteDatabase {
        return delegate.readableSupportDatabase
    }

    override fun close() {
        delegate.close()
    }

    internal class OpenHelper(
        context: Context?, name: String?,
        /**
         * This is used as an Object reference so that we can access the wrapped database inside
         * the constructor. SQLiteOpenHelper requires the error handler to be passed in the
         * constructor.
         */
        private val mDbRef: Array<FrameworkSQLiteDatabase?>,
        private val mCallback: SupportSQLiteOpenHelper.Callback
    ) : SQLiteOpenHelper(context, name, null, mCallback.version,
        DatabaseErrorHandler { dbObj ->
            mCallback.onCorruption(
                getWrappedDb(
                    mDbRef, dbObj
                )
            )
        }) {
        // see b/78359448
        private var mMigrated = false

        // there might be a connection w/ stale structure, we should re-open.
        @Suppress("RecursivePropertyAccessor")
        @get:Synchronized
        val writableSupportDatabase: SupportSQLiteDatabase
            get() {
                mMigrated = false
                val db = super.getWritableDatabase()
                if (mMigrated) {
                    // there might be a connection w/ stale structure, we should re-open.
                    close()
                    return writableSupportDatabase
                }
                return getWrappedDb(db)
            }

        // there might be a connection w/ stale structure, we should re-open.
        @Suppress("RecursivePropertyAccessor")
        @get:Synchronized
        val readableSupportDatabase: SupportSQLiteDatabase
            get() {
                mMigrated = false
                val db = super.getReadableDatabase()
                if (mMigrated) {
                    // there might be a connection w/ stale structure, we should re-open.
                    close()
                    return readableSupportDatabase
                }
                return getWrappedDb(db)
            }

        private fun getWrappedDb(sqLiteDatabase: SQLiteDatabase?): FrameworkSQLiteDatabase {
            return getWrappedDb(mDbRef, sqLiteDatabase)
        }

        override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
            mCallback.onCreate(getWrappedDb(sqLiteDatabase))
        }

        override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            mMigrated = true
            mCallback.onUpgrade(getWrappedDb(sqLiteDatabase), oldVersion, newVersion)
        }

        override fun onConfigure(db: SQLiteDatabase) {
            mCallback.onConfigure(getWrappedDb(db))
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            mMigrated = true
            mCallback.onDowngrade(getWrappedDb(db), oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase) {
            if (!mMigrated) {
                // if we've migrated, we'll re-open the db so we should not call the callback.
                mCallback.onOpen(getWrappedDb(db))
            }
        }

        @Synchronized
        override fun close() {
            super.close()
            mDbRef[0] = null
        }

        companion object {
            fun getWrappedDb(
                refHolder: Array<FrameworkSQLiteDatabase?>,
                sqLiteDatabase: SQLiteDatabase?
            ): FrameworkSQLiteDatabase {
                val dbRef = refHolder[0]
                if (dbRef == null || !dbRef.isDelegate(sqLiteDatabase!!)) {
                    refHolder[0] = FrameworkSQLiteDatabase(sqLiteDatabase!!)
                }
                return refHolder[0]!!
            }
        }
    }
}

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

import android.database.sqlite.SQLiteStatement
import androidx.sqlite.db.SupportSQLiteStatement

/**
 * Delegates all calls to a [SQLiteStatement].
 *
 * @param mDelegate The SQLiteStatement to delegate calls to.
 */
internal class FrameworkSQLiteStatement(private val mDelegate: SQLiteStatement) : FrameworkSQLiteProgram(
    mDelegate
), SupportSQLiteStatement {
    override fun execute() {
        mDelegate.execute()
    }

    override fun executeUpdateDelete(): Int {
        return mDelegate.executeUpdateDelete()
    }

    override fun executeInsert(): Long {
        return mDelegate.executeInsert()
    }

    override fun simpleQueryForLong(): Long {
        return mDelegate.simpleQueryForLong()
    }

    override fun simpleQueryForString(): String {
        return mDelegate.simpleQueryForString()
    }
}

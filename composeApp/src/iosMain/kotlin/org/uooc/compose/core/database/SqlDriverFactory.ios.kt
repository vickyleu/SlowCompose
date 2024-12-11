package org.uooc.compose.core.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.scope.Scope
import org.uooc.compose.core.database.core.database.AppDatabase

actual fun Scope.sqlDriverFactory(): SqlDriver {
    return NativeSqliteDriver(schema = AppDatabase.Schema.synchronous(), name = "${DatabaseConstants.name}.db")
}
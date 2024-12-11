package org.uooc.compose.core.database

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.scope.Scope
import org.uooc.compose.core.database.core.database.AppDatabase

expect fun Scope.sqlDriverFactory(): SqlDriver
fun createDatabase(driver: SqlDriver): AppDatabase {
    val database = AppDatabase(
        driver = driver,
    )
    return database
}
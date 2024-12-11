package org.uooc.compose.core.database.di

import org.koin.dsl.module
import org.uooc.compose.core.database.createDatabase
import org.uooc.compose.core.database.dao.UoocDao
import org.uooc.compose.core.database.dao.ViewRecordDao
import org.uooc.compose.core.database.sqlDriverFactory
import org.uooc.compose.data.di.module.BaijiaLive

val databaseModule = module {
    factory { sqlDriverFactory() }
    single { createDatabase(driver = get()) }
    single { UoocDao(appDatabase = get()) }
    single { ViewRecordDao(appDatabase = get()) }
    single { BaijiaLive() }

}
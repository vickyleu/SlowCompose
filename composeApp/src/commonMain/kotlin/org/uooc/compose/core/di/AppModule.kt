package org.uooc.compose.core.di

import coil3.PlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.uooc.compose.core.database.di.databaseModule

fun initUoocKoin(enableNetworkLogs: Boolean = false,platformContext: PlatformContext,appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            module{
                single { CoroutineScope(Job()) }
            },
            databaseModule,
        )
    }

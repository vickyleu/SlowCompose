package org.uooc.compose.core

import kotlinx.coroutines.CoroutineDispatcher

interface UoocDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

expect val uoocDispatchers: UoocDispatchers
package com.android.tool

import com.intellij.openapi.application.UI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThreadUtils {
    companion object {
        fun thread(): CoroutineScope {
            return CoroutineScope(Dispatchers.Default)
        }

        fun threadIO(): CoroutineScope {
            return CoroutineScope(Dispatchers.IO)
        }

        fun threadUI(): CoroutineScope {
            return CoroutineScope(Dispatchers.UI)
        }

        fun threadMain(): CoroutineScope {
            return CoroutineScope(Dispatchers.Main)
        }

        suspend fun <T> thread(block: suspend CoroutineScope.() -> T) {
            withContext(Dispatchers.Default, block)
        }

        suspend fun <T> threadIO(block: suspend CoroutineScope.() -> T) {
            withContext(Dispatchers.IO, block)
        }

        suspend fun <T> threadUI(block: suspend CoroutineScope.() -> T) {
            withContext(Dispatchers.UI, block)
        }

        suspend fun <T> threadMain(block: suspend CoroutineScope.() -> T) {
            withContext(Dispatchers.Main, block)
        }
    }
}
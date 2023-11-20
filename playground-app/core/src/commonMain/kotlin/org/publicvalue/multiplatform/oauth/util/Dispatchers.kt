package org.publicvalue.multiplatform.oauth.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

interface DispatcherProvider {
    fun main(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
    fun unconfined(): CoroutineDispatcher
    fun default(): CoroutineDispatcher
}

@me.tatarka.inject.annotations.Inject
class DispatcherProviderImpl() : DispatcherProvider {
    override fun main(): CoroutineDispatcher {
        return Dispatchers.Main
    }

    override fun io(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    override fun unconfined(): CoroutineDispatcher {
        return Dispatchers.Unconfined
    }

    override fun default(): CoroutineDispatcher {
        return Dispatchers.Default
    }
}
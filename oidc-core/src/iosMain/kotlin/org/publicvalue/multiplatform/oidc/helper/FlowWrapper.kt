package org.publicvalue.multiplatform.oidc.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// see https://proandroiddev.com/writing-swift-friendly-kotlin-multiplatform-apis-part-ix-flow-d4b6ada59395
public class FlowWrapper<out T> internal constructor(
    private val scope: CoroutineScope,
    private val flow: Flow<T>
) {
    private var job: Job? = null
    private var isCancelled = false

    /**
     *  Cancels the flow
     */
    @Suppress("unused")
    public fun cancel() {
        isCancelled = true
        job?.cancel()
    }

    /**
     * Starts the flow
     * @param onEach callback called on each emission
     * @param onCompletion callback called when flow completes. It will be provided with a non
     * nullable Throwable if it completes abnormally
     */
    @Suppress("unused")
    public fun collect(
        onEach: (T) -> Unit,
        onCompletion: (Throwable?) -> Unit
    ) {
        if (isCancelled) return
        job = scope.launch {
            flow.onEach(onEach).onCompletion { cause: Throwable? -> onCompletion(cause) }.collect {}
        }
    }
}

public fun <T> Flow<T>.wrap(scope: CoroutineScope = MainScope()): FlowWrapper<T> = FlowWrapper(scope, this)

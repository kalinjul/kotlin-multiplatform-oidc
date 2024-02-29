package org.publicvalue.multiplatform.oidc.appsupport

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach

/**
 * Register for activity result and return an @ActivityResultLauncherSuspend, so result can be
 * consumed in a suspend function.
 */
fun <Input, Output> ComponentActivity.registerForActivityResultSuspend(
    resultFlow: MutableStateFlow<Output?>,
    contract: ActivityResultContract<Input, Output>,
): ActivityResultLauncherSuspend<Input, Output> {

    println("###### Registering for result $this")
    println("######  resultFlow $resultFlow")
    val delegate = registerForActivityResult(contract) {
        println("###### Received result $this")
        println("######  resultFlow $resultFlow")
        println("######  result $it")
        resultFlow.value = it
    }

    return ActivityResultLauncherSuspend(
        delegate = delegate,
        resultFlow = resultFlow
    )
}

class ActivityResultLauncherSuspend<Input, Output>(
    val delegate: ActivityResultLauncher<Input>,
    val resultFlow: MutableStateFlow<Output?>,
) : ActivityResultLauncher<Input>() {

    override fun launch(input: Input, options: ActivityOptionsCompat?) {
        println("###### Launching for result $this")
        delegate.launch(input, options)
    }

    suspend fun launchSuspend(input: Input, options: ActivityOptionsCompat? = null): Output {
        println("###### Launching for result (suspend) $this")
        println("######  resultFlow $resultFlow")
        delegate.launch(input, options)
        println("###### Awaiting result $this")
        println("######  resultFlow $resultFlow")
        val result = resultFlow
            .onEach {
               println("###### Emitted result $this")
                println("######  resultFlow $resultFlow")
                println("######  result $it")
            }.filterNotNull().first()
        resultFlow.value = null
        return result
    }

    override fun unregister() {
        delegate.unregister()
    }

    override fun getContract(): ActivityResultContract<Input, *> {
        return delegate.contract
    }
}
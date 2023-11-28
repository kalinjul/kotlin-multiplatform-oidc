package org.publicvalue.multiplatform.oidc.appsupport

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * Register for activity result and return an @ActivityResultLauncherSuspend, so result can be
 * consumed in a suspend function.
 */
fun <Input, Output> ComponentActivity.registerForActivityResultSuspend(contract: ActivityResultContract<Input, Output>): ActivityResultLauncherSuspend<Input, Output> {

    val resultFlow:MutableStateFlow<Output?> = MutableStateFlow(null)
    val delegate = registerForActivityResult(contract) {
        resultFlow.value = it
    }

    val launcher = ActivityResultLauncherSuspend(
        delegate = delegate,
        resultFlow = resultFlow
    )

    return launcher
}

class ActivityResultLauncherSuspend<Input, Output>(
    val delegate: ActivityResultLauncher<Input>,
    val resultFlow: MutableStateFlow<Output?>
): ActivityResultLauncher<Input>() {

    override fun launch(input: Input, options: ActivityOptionsCompat?) {
        delegate.launch(input, options)
    }

    suspend fun launchSuspend(input: Input, options: ActivityOptionsCompat? = null): Output {
        delegate.launch(input, options)
        return resultFlow.drop(1).filterNotNull().first()
    }

    override fun unregister() {
        delegate.unregister()
    }

    override fun getContract(): ActivityResultContract<Input, *> {
        return delegate.contract
    }
}
package org.publicvalue.multiplatform.oidc.appsupport

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * Register for activity result and return an @ActivityResultLauncherSuspend, so result can be
 * consumed in a suspend function.
 *
 * @param resultFlow a MutableStateFlow(null) that should live longer than the activity
 * @param contract the contract
 */
fun <Input, Output> ComponentActivity.registerForActivityResultSuspend(
    resultFlow: MutableStateFlow<Output?> = MutableStateFlow(null),
    contract: ActivityResultContract<Input, Output>
): ActivityResultLauncherSuspend<Input, Output> {

    val delegate = registerForActivityResult(contract) {
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
): ActivityResultLauncher<Input>() {

    override fun launch(input: Input, options: ActivityOptionsCompat?) {
        delegate.launch(input, options)
    }

    suspend fun launchSuspend(input: Input, options: ActivityOptionsCompat? = null): Output {
        delegate.launch(input, options)
        val result = resultFlow.filterNotNull().first()
        resultFlow.value = null
        return result
    }

    override fun unregister() {
        delegate.unregister()
    }

    override val contract: ActivityResultContract<Input, *> = delegate.contract
}
package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * Register for activity result and return an @ActivityResultLauncherSuspend, so result can be
 * consumed in a suspend function.
 */
fun ComponentActivity.registerForActivityResultSuspend(contract: ActivityResultContracts.StartActivityForResult): ActivityResultLauncherSuspend<Intent> {

    val resultFlow:MutableStateFlow<ActivityResult?> = MutableStateFlow(null)
    val delegate = registerForActivityResult(contract) {
        resultFlow.value = it
    }

    val launcher = ActivityResultLauncherSuspend(
        delegate = delegate,
        resultFlow = resultFlow
    )

    return launcher
}

class ActivityResultLauncherSuspend<T>(
    val delegate: ActivityResultLauncher<T>,
    val resultFlow: MutableStateFlow<ActivityResult?>
): ActivityResultLauncher<T>() {

    override fun launch(input: T, options: ActivityOptionsCompat?) {
        delegate.launch(input, options)
    }

    suspend fun launchSuspend(input: T, options: ActivityOptionsCompat? = null): ActivityResult {
        delegate.launch(input, options)
        return resultFlow.drop(1).filterNotNull().first()
    }

    override fun unregister() {
        delegate.unregister()
    }

    override fun getContract(): ActivityResultContract<T, *> {
        return delegate.contract
    }
}
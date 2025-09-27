package org.publicvalue.multiplatform.oidc.appsupport.util

import android.app.Activity
import androidx.activity.result.ActivityResult
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.appsupport.WebAuthenticationFlowResult

internal fun ActivityResult.toAuthenticationFlowResult(): WebAuthenticationFlowResult {
    return when (this.resultCode) {
        Activity.RESULT_OK -> WebAuthenticationFlowResult.Success(this.data?.data?.let { Url(it.toString()) })
        else -> WebAuthenticationFlowResult.Cancelled
    }
}
package org.publicvalue.multiplatform.oidc.types

import io.ktor.http.Url
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "EndSessionRequest", name = "EndSessionRequest", exact = true)
data class EndSessionRequest(
    val url: Url
)
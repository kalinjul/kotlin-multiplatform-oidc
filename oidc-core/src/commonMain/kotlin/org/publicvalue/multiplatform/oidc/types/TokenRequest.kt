package org.publicvalue.multiplatform.oidc.types

import io.ktor.client.statement.HttpStatement
import io.ktor.http.Parameters
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "TokenRequest", name = "TokenRequest", exact = true)
public data class TokenRequest(
    public val request: HttpStatement,
    public val formParameters: Parameters
)

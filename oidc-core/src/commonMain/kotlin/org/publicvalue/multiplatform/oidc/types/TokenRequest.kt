package org.publicvalue.multiplatform.oidc.types

import io.ktor.client.statement.HttpStatement
import io.ktor.http.Parameters

data class TokenRequest(
    val request: HttpStatement,
    val formParameters: Parameters
)
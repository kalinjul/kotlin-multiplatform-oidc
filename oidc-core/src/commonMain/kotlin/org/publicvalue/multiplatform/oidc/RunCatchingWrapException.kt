package org.publicvalue.multiplatform.oidc

public inline fun <R> wrapExceptions(block: () -> R): R {
    return try {
        block()
    } catch (e: OpenIdConnectException) {
        throw e
    } catch (e: Throwable) {
        throw OpenIdConnectException.TechnicalFailure(e.message ?: "Unknown error", e)
    }
}

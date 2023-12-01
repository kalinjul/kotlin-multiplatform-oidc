package org.publicvalue.multiplatform.oidc

inline fun <R> wrapExceptions(block: () -> R): R {
    return try {
        block()
    } catch (e: OpenIDConnectException) {
        throw e
    } catch (e: Throwable) {
        throw OpenIDConnectException.TechnicalFailure(e.message ?: "Unknown error", e)
    }
}
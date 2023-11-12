package org.publicvalue.multiplatform.oidc


sealed class OpenIDConnectException(
    override val message: String,
    override val cause: Throwable? = null
): Exception(message, cause) {

    data class InvalidUrl(val url: String?, override val cause: Throwable? = null): OpenIDConnectException(message = "Invalid URL: $url", cause = cause)
    data class InvalidOrMissingRedirectUri(val redirectUrl: String?): OpenIDConnectException(message = "invalid or missing redirect url: $redirectUrl")
    data class AuthenticationFailed(override val cause: Throwable): OpenIDConnectException(message = "Authentication failed.", cause = cause)

    enum class Type {
        invalidTokenPostData,
        unsuccessfulTokenRequest,
        internalError,
    }

//    case .invalidOrMissingRedirectUri:
//    return "invalid or missing redirect url"
//    case .invalidTokenPostData:
//    return "invalid token post data"
//    case .responseIsNotOfTypeHttpResponse:
//    return "internal type error: the response is not of type http response. This must not happen!"
//    case .unsuccessfulTokenRequest(let statusCode, let description):
//    return "token request failed!\nStatusCode: \(statusCode)\nDescription: \(description)"
//    case .internalError(let message):
//    return "tnternal error: \(message)"
//}
}

package org.publicvalue.multiplatform.oauth.domain

import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oidc.types.remote.OpenIDConnectConfiguration

fun Identityprovider.updateWith(config: OpenIDConnectConfiguration): Identityprovider =
    copy(
        endpointToken = config.token_endpoint,
        endpointAuthorization = config.authorization_endpoint,
        endpointDeviceAuthorization = config.device_authorization_endpoint,
        endpointIntrospection = config.introspection_endpoint,
        endpointUserInfo = config.userinfo_endpoint,
        endpointEndSession = config.end_session_endpoint
    )

package org.publicvalue.multiplatform.oauth.domain

import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oidc.types.remote.OpenIdConnectConfiguration

fun Identityprovider.updateWith(config: OpenIdConnectConfiguration): Identityprovider =
    copy(
        endpointToken = config.tokenEndpoint,
        endpointAuthorization = config.authorizationEndpoint,
        endpointDeviceAuthorization = config.deviceAuthorizationEndpoint,
        endpointIntrospection = config.introspectionEndpoint,
        endpointUserInfo = config.userinfoEndpoint,
        endpointEndSession = config.endSessionEndpoint
    )

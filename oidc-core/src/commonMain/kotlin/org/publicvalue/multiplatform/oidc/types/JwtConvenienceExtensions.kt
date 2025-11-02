package org.publicvalue.multiplatform.oidc.types

import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

// swift convenience overloads with default parameters for suspend functions

@OptIn(ExperimentalObjCName::class)
@ObjCName("JwtParser", "JwtParser", exact = true)
public object JwtParser {
    /**
     * Objective-C convenience function
     * @see Jwt.parse
     */
    @Throws(OpenIdConnectException::class)
    public fun parse(from: String): Jwt = Jwt.parse(from)
}

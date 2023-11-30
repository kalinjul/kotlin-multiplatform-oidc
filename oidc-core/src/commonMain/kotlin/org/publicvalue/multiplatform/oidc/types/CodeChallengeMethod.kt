package org.publicvalue.multiplatform.oidc.types

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "CodeChallengeMethod", name = "CodeChallengeMethod", exact = true)
enum class CodeChallengeMethod(
    val queryString: String?
) {
    S256("S256"), plain("plain"), off(null)
}
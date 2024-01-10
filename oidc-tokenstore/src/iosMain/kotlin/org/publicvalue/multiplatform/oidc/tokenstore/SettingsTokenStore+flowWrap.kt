package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.helper.FlowWrapper
import org.publicvalue.multiplatform.oidc.helper.wrap
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCName::class)
@ObjCName("accessToken")
@Suppress("unused")
val TokenStore.accessTokenFlowWrap: FlowWrapper<String?> get() = this.accessTokenFlow.wrap()

@OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCName::class)
@ObjCName("refreshToken")
@Suppress("unused")
val TokenStore.refreshTokenFlowWrap: FlowWrapper<String?> get() = this.refreshTokenFlow.wrap()

@OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCName::class)
@ObjCName("idToken")
@Suppress("unused")
val TokenStore.idTokenFlowWrap: FlowWrapper<String?> get() = this.idTokenFlow.wrap()
package org.publicvalue.multiplatform.oauth.domain.inject

import me.tatarka.inject.annotations.Provides
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SimpleKtorWebserver
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver

interface WebserverComponent {

    @OptIn(ExperimentalOpenIdConnect::class)
    @ApplicationScope
    @Provides
    fun provideWebserver(): Webserver {
        return SimpleKtorWebserver()
    }
}
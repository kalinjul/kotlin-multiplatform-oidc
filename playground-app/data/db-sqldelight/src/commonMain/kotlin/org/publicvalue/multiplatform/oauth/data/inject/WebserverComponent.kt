package org.publicvalue.multiplatform.oauth.data.inject

import app.cash.sqldelight.db.SqlDriver
import me.tatarka.inject.annotations.Provides
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SimpleKtorWebserver
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver

interface WebserverComponent {
    @ApplicationScope
    @Provides
    fun provideWebserver(
        driver: SqlDriver,
    ): Webserver {
        return SimpleKtorWebserver()
    }
}

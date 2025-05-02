package org.publicvalue.multiplatform.oauth.domain

object Constants {
    val WEBSERVER_PORT = 8080
    val REDIRECT_URL = "http://localhost:$WEBSERVER_PORT/redirect"
    val POST_LOGOUT_REDIRECT_URL: String = "http://localhost:$WEBSERVER_PORT/logout"
}
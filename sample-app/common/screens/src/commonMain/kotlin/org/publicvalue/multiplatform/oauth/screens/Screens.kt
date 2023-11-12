package org.publicvalue.multiplatform.oauth.screens

import com.slack.circuit.runtime.screen.Screen

@CommonParcelize
object IdpListScreen : AppScreen(name = "IdpList()")

@CommonParcelize
class ClientListScreen(val idpId: Long) : AppScreen(name = "ClientList()") {
    override val arguments: Map<String, *>?
        get() = mapOf("idpId" to idpId)
}

@CommonParcelize
class ClientDetailScreen(val clientId: Long) : AppScreen(name = "ClientDetail()") {
    override val arguments: Map<String, *>?
        get() = mapOf("clientId" to clientId)
}

abstract class AppScreen(val name: String) : Screen {
    open val arguments: Map<String, *>? = null
}

fun Screen.isRootScreen() = when (this) {
    is IdpListScreen -> true
    else -> false
}
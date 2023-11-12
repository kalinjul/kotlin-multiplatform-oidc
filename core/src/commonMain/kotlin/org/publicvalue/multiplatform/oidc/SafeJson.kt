package org.publicvalue.multiplatform.oidc

import kotlinx.serialization.json.Json

//object SafeJson {
//    inline fun <reified T> decodeFromString(s: String): T? {
//        return try {
//            Json.decodeFromString(s)
//        } catch (e: Exception) {
//            // TODO log
//            e.printStackTrace()
//            null
//        }
//    }
//}
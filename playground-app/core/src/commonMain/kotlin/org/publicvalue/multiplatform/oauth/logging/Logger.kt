package org.publicvalue.multiplatform.oauth.logging

import org.publicvalue.multiplatform.oauth.inject.ApplicationScope
import me.tatarka.inject.annotations.Inject


interface Logger {
    enum class Level {
        VERBOSE, DEBUG, INFO, ERROR, WARN
    }
    fun v(throwable: Throwable? = null, message: () -> String = { "" }) = log(Level.VERBOSE, throwable, message)
    fun d(throwable: Throwable? = null, message: () -> String = { "" }) = log(Level.DEBUG, throwable, message)
    fun i(throwable: Throwable? = null, message: () -> String = { "" }) = log(Level.INFO, throwable, message)
    fun e(throwable: Throwable? = null, message: () -> String = { "" }) = log(Level.ERROR, throwable, message)
    fun w(throwable: Throwable? = null, message: () -> String = { "" }) = log(Level.WARN, throwable, message)

    fun log(level: Level, throwable: Throwable? = null, message: () -> String = { "" })
}

@Inject
@ApplicationScope
class StdoutLogger(): Logger {
    override fun log(level: Logger.Level, throwable: Throwable?, message: () -> String) {
        println("$level: ${message()}")
        throwable?.printStackTrace() // TODO platform specific logging with implementation 'io.github.oshai:kotlin-logging-jvm:5.1.0'
    }
}
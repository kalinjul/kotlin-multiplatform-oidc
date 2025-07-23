package presentation.state

import androidx.compose.runtime.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DebugLogger {
    private val _logs = mutableStateListOf<String>()
    val logs: List<String> = _logs

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun log(message: String, tag: String = "Debug") {
        val timestamp = LocalTime.now().format(timeFormatter)
        val logMessage = "[$timestamp] $tag: $message"
        _logs.add(logMessage)
        println(logMessage)
    }

    fun clear() {
        _logs.clear()
    }

    fun logSuccess(message: String) {
        log("✅ $message", "Success")
    }

    fun logError(message: String, throwable: Throwable? = null) {
        log("❌ $message", "Error")
        throwable?.let { log("Stack trace: ${it.message}", "Error") }
    }

    fun logInfo(message: String) {
        log("ℹ️ $message", "Info")
    }

    fun logWarning(message: String) {
        log("⚠️ $message", "Warning")
    }
}

@Composable
fun rememberDebugLogger(): DebugLogger {
    return remember { DebugLogger() }
}

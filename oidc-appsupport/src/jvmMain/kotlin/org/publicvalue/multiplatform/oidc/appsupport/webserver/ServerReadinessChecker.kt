package org.publicvalue.multiplatform.oidc.appsupport.webserver

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import kotlinx.coroutines.delay
import java.net.Socket
import java.net.ConnectException

/**
 * Handles server readiness validation and connection testing.
 * 
 * Provides utilities for verifying that a server is ready to accept connections
 * before considering the startup process complete. This prevents timing issues
 * where clients attempt to connect before the server is fully initialized.
 */
@ExperimentalOpenIdConnect
object ServerReadinessChecker {
    
    /**
     * Check if server is ready to accept connections by attempting a connection test.
     * 
     * This is more reliable than arbitrary delays as it actively tests server availability.
     * 
     * @param port Port to test for server readiness
     * @param host Host to test (defaults to localhost)
     * @param maxWaitMs Maximum time to wait for server readiness
     * @param delayMs Delay between connection attempts
     */
    suspend fun waitForServerReady(
        port: Int,
        host: String = "localhost",
        maxWaitMs: Long = 5000,
        delayMs: Long = 10
    ) {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < maxWaitMs) {
            if (isServerReady(port, host)) {
                return
            }
            delay(delayMs)
        }
    }
    
    /**
     * Test if a server is ready by attempting a connection.
     * 
     * @param port Port to test
     * @param host Host to test
     * @return true if server is ready, false otherwise
     */
    private fun isServerReady(port: Int, host: String = "localhost"): Boolean {
        return try {
            Socket(host, port).use { 
                // If we can connect, server is ready
                true
            }
        } catch (e: ConnectException) {
            // Server not ready yet
            false
        } catch (e: Exception) {
            true
        }
    }
    
    /**
     * Wait for multiple servers to be ready.
     * 
     * @param ports List of ports to check
     * @param host Host to test
     * @param maxWaitMs Maximum time to wait for all servers
     */
    suspend fun waitForServersReady(
        ports: List<Int>,
        host: String = "localhost",
        maxWaitMs: Long = 10000
    ) {
        for (port in ports) {
            waitForServerReady(port, host, maxWaitMs / ports.size)
        }
    }
    
    /**
     * Check if a specific port is available (not in use).
     * 
     * @param port Port to check
     * @param host Host to check
     * @return true if port is available, false if in use
     */
    fun isPortAvailable(port: Int, host: String = "localhost"): Boolean {
        return try {
            Socket(host, port).use { false }
        } catch (e: ConnectException) {
            // Port is available
            true
        } catch (e: Exception) {
            // Port might be in use or other issue
            false
        }
    }
}
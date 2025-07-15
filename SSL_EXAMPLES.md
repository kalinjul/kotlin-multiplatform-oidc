# SSL Support Examples for Kotlin Multiplatform OIDC

This document provides comprehensive examples of SSL/TLS support features for the JVM implementation of the Kotlin Multiplatform OIDC library.

## Table of Contents

1. [HTTP Client SSL Configuration](#http-client-ssl-configuration)
2. [HTTPS Local Redirect Server](#https-local-redirect-server)
3. [Certificate Source Types](#certificate-source-types)
4. [Complete Working Examples](#complete-working-examples)
5. [Development Setup with Dual-Service Architecture](#development-setup-with-dual-service-architecture)
6. [Docker Integration](#docker-integration)
7. [Certificate Management](#certificate-management)
8. [Security Considerations](#security-considerations)
9. [Troubleshooting](#troubleshooting)

## HTTP Client SSL Configuration

All SSL features require the `@ExperimentalOpenIdConnect` annotation.

### Basic SSL Configuration with Trust Store

```kotlin
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig
import org.publicvalue.multiplatform.oidc.ssl.createSslEnabledOpenIdConnectClient
import org.publicvalue.multiplatform.oidc.ssl.ssl
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

@OptIn(ExperimentalOpenIdConnect::class)
fun createClientWithTrustStore(): OpenIdConnectClient {
    val config = OpenIdConnectClientConfig("https://example.com/.well-known/openid-configuration").apply {
        clientId = "your-client-id"
        clientSecret = "your-client-secret"
        
        ssl {
            trustStore("/path/to/custom-truststore.jks", "truststore-password")
        }
    }
    
    return createSslEnabledOpenIdConnectClient(config)
}
```

### SSL Configuration with Client Certificate Authentication

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
fun createClientWithClientCertificate(): OpenIdConnectClient {
    val config = OpenIdConnectClientConfig("https://example.com/.well-known/openid-configuration").apply {
        clientId = "your-client-id"
        
        ssl {
            keyStore("/path/to/client-keystore.p12", "keystore-password")
            trustStore("/path/to/truststore.jks", "truststore-password")
        }
    }
    
    return createSslEnabledOpenIdConnectClient(config)
}
```

### Disable SSL Validation (Development Only)

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
fun createUnsafeClient(): OpenIdConnectClient {
    val config = OpenIdConnectClientConfig("https://example.com/.well-known/openid-configuration").apply {
        clientId = "your-client-id"
        clientSecret = "your-client-secret"
        
        ssl {
            disableCertificateValidation()
            disableHostnameVerification()
        }
    }
    
    return createSslEnabledOpenIdConnectClient(config)
}
```

### Custom Trust Manager

```kotlin
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

@OptIn(ExperimentalOpenIdConnect::class)
fun createClientWithCustomTrustManager(): OpenIdConnectClient {
    val customTrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }
    
    val config = OpenIdConnectClientConfig("https://example.com/.well-known/openid-configuration").apply {
        clientId = "your-client-id"
        
        ssl {
            trustManager(customTrustManager)
        }
    }
    
    return createSslEnabledOpenIdConnectClient(config)
}
```

### Adding Trusted Certificates

```kotlin
import java.security.cert.CertificateFactory
import java.io.File

@OptIn(ExperimentalOpenIdConnect::class)
fun createClientWithTrustedCertificate(): OpenIdConnectClient {
    // Load a certificate file
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val caCert = File("path/to/ca-certificate.crt").inputStream().use { inputStream ->
        certificateFactory.generateCertificate(inputStream) as X509Certificate
    }
    
    val config = OpenIdConnectClientConfig("https://example.com/.well-known/openid-configuration").apply {
        clientId = "your-client-id"
        
        ssl {
            addTrustedCertificate(caCert)
            disableHostnameVerification() // Often needed for demo certificates
        }
    }
    
    return createSslEnabledOpenIdConnectClient(config)
}
```

## HTTPS Local Redirect Server

### Basic HTTPS Redirect Server

```kotlin
import org.publicvalue.multiplatform.oidc.appsupport.JvmCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SslWebserver
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateSourceFactory

@OptIn(ExperimentalOpenIdConnect::class)
fun createHttpsFactory(): JvmCodeAuthFlowFactory {
    return JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = CertificateSourceFactory.selfSigned()
            )
        }
    )
}
```

### HTTPS with File-based Certificate

```kotlin
import java.io.File

@OptIn(ExperimentalOpenIdConnect::class)
fun createHttpsFactoryWithCertificate(): JvmCodeAuthFlowFactory {
    val certificateFile = File("/path/to/localhost-cert.p12")
    
    return JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = CertificateSourceFactory.fromFile(
                    certificateFile = certificateFile,
                    password = "certificate-password"
                )
            )
        }
    )
}
```

### HTTPS with Auto-Detection

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
fun createHttpsFactoryWithAutoDetection(): JvmCodeAuthFlowFactory {
    return JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = CertificateSourceFactory.autoDetect()
            )
        }
    )
}
```

### HTTP Fallback (Default Behavior)

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
fun createHttpFactory(): JvmCodeAuthFlowFactory {
    return JvmCodeAuthFlowFactory(port = 8080)
}
```

## Certificate Source Types

### Self-Signed Certificates

```kotlin
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateSourceFactory

@OptIn(ExperimentalOpenIdConnect::class)
fun createSelfSignedCertificateSource(): CertificateSource {
    return CertificateSourceFactory.selfSigned(
        validityDays = 365L,
        password = "localhost"
    )
}
```

### File-based Certificates

```kotlin
import java.io.File

@OptIn(ExperimentalOpenIdConnect::class)
fun createFileCertificateSource(): CertificateSource {
    return CertificateSourceFactory.fromFile(
        certificateFile = File("/path/to/certificate.p12"),
        password = "certificate-password",
        alias = "localhost"
    )
}
```

### Resources-based Certificates

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
fun createResourcesCertificateSource(): CertificateSource {
    // Auto-detect certificates in resources
    return CertificateSourceFactory.fromResources()
}

@OptIn(ExperimentalOpenIdConnect::class)
fun createSpecificResourcesCertificateSource(): CertificateSource {
    // Specific resource certificate
    return CertificateSourceFactory.fromResources(
        resourcePath = "/ssl/certificates/localhost.p12",
        password = "localhost",
        alias = "localhost"
    )
}
```

### Auto-Detection

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
fun createAutoDetectCertificateSource(): CertificateSource {
    return CertificateSourceFactory.autoDetect(
        hostname = "localhost",
        password = "localhost"
    )
}
```

## Complete Working Examples

### Basic SSL Client with HTTPS Redirect

```kotlin
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig
import org.publicvalue.multiplatform.oidc.ssl.createSslEnabledOpenIdConnectClient
import org.publicvalue.multiplatform.oidc.ssl.ssl
import org.publicvalue.multiplatform.oidc.appsupport.JvmCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SslWebserver
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateSourceFactory
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

@OptIn(ExperimentalOpenIdConnect::class)
suspend fun performOAuthWithSsl() {
    // Configure HTTPS client
    val config = OpenIdConnectClientConfig("https://corporate-sso.example.com/.well-known/openid-configuration").apply {
        clientId = "corporate-app"
        clientSecret = "app-secret"
        redirectUri = "https://localhost:8443/redirect"
        
        ssl {
            trustStore("/etc/ssl/corporate-truststore.jks", "truststore-password")
        }
    }
    
    val client = createSslEnabledOpenIdConnectClient(config)
    
    // Configure HTTPS redirect server
    val factory = JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = CertificateSourceFactory.autoDetect()
            )
        }
    )
    
    // Perform authentication
    val authFlow = factory.createAuthFlow(client)
    val tokens = authFlow.getAccessToken()
    
    println("Access Token: ${tokens.access_token}")
}
```

### Corporate Environment with Client Certificate

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
suspend fun performMutualTlsAuth() {
    val config = OpenIdConnectClientConfig("https://secure-idp.corp.com/.well-known/openid-configuration").apply {
        clientId = "mtls-client"
        redirectUri = "https://localhost:8443/redirect"
        
        ssl {
            keyStore("/etc/ssl/client-cert.p12", "client-password")
            trustStore("/etc/ssl/corporate-truststore.jks", "truststore-password")
        }
    }
    
    val client = createSslEnabledOpenIdConnectClient(config)
    
    val factory = JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = CertificateSourceFactory.fromFile(
                    certificateFile = File("/etc/ssl/localhost.p12"),
                    password = "localhost-password"
                )
            )
        }
    )
    
    val authFlow = factory.createAuthFlow(client)
    val tokens = authFlow.getAccessToken()
    
    println("mTLS Authentication successful")
}
```

### Development with Resources Certificate

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
suspend fun performDevAuth() {
    val config = OpenIdConnectClientConfig("https://dev-idp.example.com/.well-known/openid-configuration").apply {
        clientId = "dev-app"
        clientSecret = "dev-secret"
        redirectUri = "https://localhost:8443/redirect"
        
        ssl {
            disableHostnameVerification()
            // Trust development certificates
        }
    }
    
    val client = createSslEnabledOpenIdConnectClient(config)
    
    val factory = JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = CertificateSourceFactory.fromResources(
                    resourcePath = "/ssl/certificates/localhost.p12",
                    password = "localhost"
                )
            )
        }
    )
    
    val authFlow = factory.createAuthFlow(client)
    val tokens = authFlow.getAccessToken()
    
    println("Development authentication successful")
}
```

## Development Setup with Dual-Service Architecture

The development environment supports both HTTP and HTTPS Keycloak services running simultaneously:

### HTTP Development Mode

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
suspend fun performHttpAuth() {
    val config = OpenIdConnectClientConfig("http://localhost:7080/realms/playground/.well-known/openid-configuration").apply {
        clientId = "basic-client"
        clientSecret = "basic-client-secret"
        redirectUri = "http://localhost:8080/redirect"
        scope = "openid profile email"
    }
    
    val client = createSslEnabledOpenIdConnectClient(config)
    
    val factory = JvmCodeAuthFlowFactory(port = 8080)
    
    val authFlow = factory.createAuthFlow(client)
    val tokens = authFlow.getAccessToken()
    
    println("HTTP Authentication successful")
}
```

### HTTPS Development Mode

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
suspend fun performHttpsAuth() {
    val config = OpenIdConnectClientConfig("https://localhost:7001/realms/playground/.well-known/openid-configuration").apply {
        clientId = "basic-client"
        clientSecret = "basic-client-secret"
        redirectUri = "https://localhost:8443/redirect"
        scope = "openid profile email"
        
        ssl {
            // Trust demo CA certificate
            val caCert = loadDemoCaCertificate()
            addTrustedCertificate(caCert)
            disableHostnameVerification()
        }
    }
    
    val client = createSslEnabledOpenIdConnectClient(config)
    
    val factory = JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = CertificateSourceFactory.autoDetect()
            )
        }
    )
    
    val authFlow = factory.createAuthFlow(client)
    val tokens = authFlow.getAccessToken()
    
    println("HTTPS Authentication successful")
}

private fun loadDemoCaCertificate(): X509Certificate {
    val caCertFile = File("docker/certs/ca/ca.crt")
    val certificateFactory = CertificateFactory.getInstance("X.509")
    return caCertFile.inputStream().use { inputStream ->
        certificateFactory.generateCertificate(inputStream) as X509Certificate
    }
}
```

## Docker Integration

### Using Docker-Generated Certificates

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
fun createFactoryWithDockerCertificates(): JvmCodeAuthFlowFactory {
    // Docker certificates are automatically copied to user directory
    val userCertFile = File(System.getProperty("user.home"), ".oidc-desktop-ssl/localhost.p12")
    
    return JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = if (userCertFile.exists()) {
                    CertificateSourceFactory.fromFile(userCertFile, "localhost")
                } else {
                    CertificateSourceFactory.autoDetect()
                }
            )
        }
    )
}
```

### Quick Start Integration

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
suspend fun performQuickStartAuth() {
    // This example works with the quick_start.sh script
    val config = OpenIdConnectClientConfig("https://localhost:7001/realms/playground/.well-known/openid-configuration").apply {
        clientId = "basic-client"
        clientSecret = "basic-client-secret"
        redirectUri = "https://localhost:8443/redirect"
        scope = "openid profile email"
        
        ssl {
            // Configure for demo environment
            configureDemoSsl()
        }
    }
    
    val client = createSslEnabledOpenIdConnectClient(config)
    
    val factory = JvmCodeAuthFlowFactory(
        port = 8443,
        webserverProvider = {
            SslWebserver(
                enableHttps = true,
                certificateSource = CertificateSourceFactory.autoDetect()
            )
        }
    )
    
    val authFlow = factory.createAuthFlow(client)
    val tokens = authFlow.getAccessToken()
    
    println("Quick start authentication successful")
}

private fun OpenIdConnectClientConfig.configureDemoSsl() {
    ssl {
        try {
            // Try to load demo CA certificate
            val caCertFile = File("docker/certs/ca/ca.crt")
            if (caCertFile.exists()) {
                val certificateFactory = CertificateFactory.getInstance("X.509")
                val caCert = caCertFile.inputStream().use { inputStream ->
                    certificateFactory.generateCertificate(inputStream) as X509Certificate
                }
                addTrustedCertificate(caCert)
            }
            disableHostnameVerification()
        } catch (e: Exception) {
            // Fallback to unsafe mode for demo
            disableCertificateValidation()
            disableHostnameVerification()
        }
    }
}
```

## Certificate Management

### Loading Certificates from Resources

```kotlin
@OptIn(ExperimentalOpenIdConnect::class)
fun loadResourceCertificate(): CertificateSource {
    // Auto-detect certificates in src/jvmMain/resources/ssl/certificates/
    return CertificateSourceFactory.fromResources()
}

@OptIn(ExperimentalOpenIdConnect::class)
fun loadSpecificResourceCertificate(): CertificateSource {
    return CertificateSourceFactory.fromResources(
        resourcePath = "/ssl/certificates/localhost.p12",
        password = "localhost",
        alias = "localhost"
    )
}
```

### Certificate Validation

```kotlin
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateValidator

@OptIn(ExperimentalOpenIdConnect::class)
fun validateCertificate() {
    val certificateFile = File("/path/to/certificate.p12")
    val isValid = CertificateValidator.validateCertificate(certificateFile, "password")
    
    if (isValid) {
        println("Certificate is valid")
    } else {
        println("Certificate validation failed")
    }
}
```

## Security Considerations

### Development vs Production

**Development (Demo Environment)**:
- Self-signed certificates
- Default passwords
- Disabled hostname verification
- Unsafe SSL options available

**Production Recommendations**:
- CA-signed certificates
- Strong, unique passwords
- Strict SSL validation
- Regular certificate rotation

### Best Practices

1. **Certificate Management**:
   - Use proper CA-signed certificates in production
   - Implement certificate rotation procedures
   - Monitor certificate expiration

2. **Password Security**:
   - Never hardcode passwords in source code
   - Use environment variables or secure configuration
   - Implement proper secret management

3. **SSL Configuration**:
   - Only disable SSL validation in development
   - Use strong cipher suites
   - Implement proper certificate validation

4. **Network Security**:
   - Use HTTPS for all communications
   - Implement proper firewall rules
   - Use VPNs for remote access

## Troubleshooting

### Common Issues

#### Certificate Trust Issues

**Problem**: Browser shows "Not Secure" warnings or SSL handshake failures

**Solution**:
```kotlin
// Add the CA certificate to trusted certificates
ssl {
    val caCert = loadCaCertificate()
    addTrustedCertificate(caCert)
    disableHostnameVerification() // For localhost development
}
```

#### Resource Certificate Loading

**Problem**: Certificates not found in resources

**Solution**:
```kotlin
// Check if certificate exists in resources
val resourceUrl = this::class.java.getResource("/ssl/certificates/localhost.p12")
if (resourceUrl != null) {
    val certificateSource = CertificateSourceFactory.fromResources(
        resourcePath = "/ssl/certificates/localhost.p12",
        password = "localhost"
    )
} else {
    // Fallback to self-signed
    val certificateSource = CertificateSourceFactory.selfSigned()
}
```

#### Auto-Detection Issues

**Problem**: Auto-detection not finding certificates

**Solution**:
```kotlin
// Check certificate locations manually
val locations = listOf(
    File(System.getProperty("user.home"), ".oidc-desktop-ssl/localhost.p12"),
    File("docker/certs/localhost/localhost.p12"),
    File("/etc/ssl/localhost.p12")
)

val certificateSource = locations.firstOrNull { it.exists() }?.let { file ->
    CertificateSourceFactory.fromFile(file, "localhost")
} ?: CertificateSourceFactory.selfSigned()
```

### Debug Commands

```bash
# Check certificate validity
openssl x509 -in certificate.crt -text -noout

# Test SSL connection
openssl s_client -connect localhost:8443 -servername localhost

# Check Docker certificates
ls -la docker/certs/localhost/
```

## Important Notes

1. **Experimental API**: All SSL features require `@OptIn(ExperimentalOpenIdConnect::class)`

2. **Self-Signed Certificates**: Browsers will show security warnings for self-signed certificates

3. **Certificate Storage**: Auto-generated certificates are stored in `~/.oidc-desktop-ssl/`

4. **Development Mode**: Use unsafe SSL options only in development environments

5. **Resource Certificates**: Certificates in resources are bundled with the JAR file

6. **Auto-Detection Priority**:
   1. Explicit certificate file parameter
   2. User-copied Docker certificates
   3. Docker-generated certificates
   4. Resource certificates
   5. Self-signed certificates

For more information, see the [sample application](sample-app/desktop-app-with-ssl/) for complete working examples.
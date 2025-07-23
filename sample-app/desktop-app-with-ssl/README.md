# OIDC SSL Sample Desktop Application

This desktop application demonstrates SSL/TLS features for the Kotlin Multiplatform OIDC library.

## Features

### 1. HTTPS Redirect Server Demo
- Shows how to enable HTTPS for the OAuth redirect server
- Demonstrates self-signed certificate usage
- Displays server status and configuration
- Includes browser security warning information

### 2. HTTP Client SSL Configuration Demo
- Interactive SSL configuration builder
- Multiple configuration scenarios:
  - Default (system trust store)
  - Custom trust store
  - Disable SSL validation (development only)
  - Client certificate authentication
- Live configuration code generation
- Connection testing capabilities

### 3. OAuth Flow Demo
- Complete OAuth authorization code flow with HTTP or HTTPS Keycloak
- SSL configuration integration for both client and server
- HTTPS Keycloak support with automatic certificate trust
- Real-time status updates and comprehensive logging
- Token display and management
- End-to-end SSL OAuth demonstration

### 4. Let's Encrypt Demo
- Automatic certificate issuance via ACME protocol
- HTTP-01, DNS-01, and TLS-ALPN-01 challenge support
- Production and staging environment options
- Certificate lifecycle management

### 5. Resources Certificate Demo
- Load certificates from application resources folder
- Bundle certificates with application JAR
- Auto-detection of available resource certificates
- Certificate validation and information display
- Perfect for distributing pre-configured certificates

### 6. Configuration Reference
- Code examples for common SSL scenarios
- Copy-paste ready configurations
- Best practices documentation

## Running the Application

```bash
cd sample-app
./gradlew :desktop-app-with-ssl:run
```

## HTTPS Keycloak Setup

The OAuth Flow Demo supports both HTTP and HTTPS Keycloak configurations:

### Dual-Service Architecture

The demo environment runs both HTTP and HTTPS Keycloak services simultaneously:

#### HTTPS Keycloak (Production-like)
- **Admin Console**: https://localhost:7001/admin/
- **Realm**: https://localhost:7001/realms/playground
- **Discovery**: https://localhost:7001/realms/playground/.well-known/openid-configuration
- **Database**: PostgreSQL on localhost:7000
- **Use case**: Production-like SSL testing, certificate validation

#### HTTP Keycloak (Development)
- **Admin Console**: http://localhost:7080/admin/
- **Realm**: http://localhost:7080/realms/playground
- **Discovery**: http://localhost:7080/realms/playground/.well-known/openid-configuration
- **Database**: PostgreSQL on localhost:7005
- **Use case**: Development, debugging, no SSL complexity

### Quick Start

1. **Generate Certificates:**
   ```bash
   cd docker
   ./generate-certs.sh
   ```

2. **Start All Services:**
   ```bash
   ./demo/quick_start.sh
   ```
   
   This starts:
   - postgres-https (port 7000)
   - postgres-http (port 7005)
   - keycloak HTTPS (port 7001)
   - keycloak-http (port 7080)

3. **Switch Between Modes:**
   - Launch the desktop application
   - Go to "OAuth Flow Demo" tab
   - Toggle "Use HTTPS for Keycloak" switch
   - The discovery URL automatically switches between HTTP and HTTPS

4. **Certificate Trust:**
   - The app automatically trusts the demo CA certificate
   - For browser access, import `docker/certs/ca/ca.crt` to your browser's trust store

## Building a Distribution

```bash
cd sample-app
./gradlew :desktop-app-with-ssl:createDistributable
```

## SSL Resources

The `src/jvmMain/resources/ssl/` directory contains:
- Sample certificate templates
- Trust store examples
- Configuration documentation

## Important Notes

**Current Implementation**: This demo application uses the published OIDC library versions and shows the SSL configuration UI concepts. The actual SSL functionality requires the enhanced versions with SSL support.

**Enhanced Version**: When using the full SSL-enabled version of the library, all demonstrated features will be fully functional.

**Security Warnings**: 
- Self-signed certificates will trigger browser security warnings
- Never disable SSL validation in production environments
- Use proper CA-signed certificates for production deployments

## SSL Configuration Examples

### Basic HTTPS Redirect
```kotlin
val factory = JvmCodeAuthFlowFactory.createWithHttps()
```

### Custom Trust Store
```kotlin
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "your-client-id"
    ssl {
        trustStore("/path/to/truststore.jks", "password")
    }
}.createSslEnabledClient()
```

### Development Mode (Disable Validation)
```kotlin
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "your-client-id"
    ssl {
        disableCertificateValidation()
        disableHostnameVerification()
    }
}.createSslEnabledClient()
```

### Client Certificate Authentication
```kotlin
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "your-client-id"
    ssl {
        keyStore("/path/to/client-cert.p12", "password")
        trustStore("/path/to/truststore.jks", "password")
    }
}.createSslEnabledClient()
```

### Resources Certificate Loading
```kotlin
// Auto-detect resource certificate
val certificateSource = CertificateSourceFactory.fromResources()
val factory = JvmCodeAuthFlowFactory.createWithCustomWebserver {
    SslWebserver.createWithCertificateSource(certificateSource)
}

// Specific resource certificate
val specificSource = CertificateSourceFactory.fromResources(
    resourcePath = "/ssl/certificates/localhost.p12",
    password = "localhost"
)
val factory = JvmCodeAuthFlowFactory.createWithCustomWebserver {
    SslWebserver.createWithCertificateSource(specificSource)
}

// Use in auto-detection (includes resources in search)
val autoSource = CertificateSourceFactory.autoDetect()
val factory = JvmCodeAuthFlowFactory.createWithCustomWebserver {
    SslWebserver.createWithCertificateSource(autoSource)
}
```

## Resources Certificate Structure

The application includes sample certificates in the `src/jvmMain/resources/ssl/` directory:

```
ssl/
├── certificates/         # Primary certificates loaded by the application
│   ├── localhost.p12     # PKCS12 certificate for localhost (password: localhost)
│   └── localhost.jks     # JKS certificate for localhost (password: localhost)
├── examples/             # Example certificates for reference
│   ├── client-cert.p12   # Sample client certificate (password: client123)
│   ├── custom-ca.crt     # Sample CA certificate
│   └── truststore.jks    # Sample truststore (password: truststore)
└── README.md             # Detailed documentation
```

### Resource Loading Priority

The application automatically detects certificates in this order:
1. `/ssl/certificates/{hostname}.p12`
2. `/ssl/certificates/{hostname}.jks`
3. `/ssl/certificates/localhost.p12`
4. `/ssl/certificates/localhost.jks`

### Adding Your Own Certificates

To add your own certificates to the resources:

1. Place your certificate files in `src/jvmMain/resources/ssl/certificates/`
2. Use supported formats: PKCS12 (.p12, .pfx) or JKS (.jks)
3. Update your code to use `CertificateSourceFactory.fromResources()`
4. The certificates will be bundled with your application JAR

## Certificate Management

### Security Notice
🔐 **All certificate files are automatically excluded from version control for security reasons.** The `.gitignore` file contains comprehensive patterns to prevent accidental commit of SSL certificates, private keys, and keystores.

### Certificate Generation
The certificates are dynamically generated using Docker containers and stored in the `docker/certs/` directory. The setup includes:

- **CA Certificate**: Root certificate authority for signing all other certificates
- **Keycloak Certificate**: Server certificate for Keycloak HTTPS
- **Client Certificate**: For mutual TLS authentication scenarios
- **PostgreSQL Certificate**: For secure database connections
- **NGINX Certificate**: For reverse proxy scenarios

All certificates are signed by the demo CA and configured for `localhost` access.

### Certificate Scripts

#### Generate Certificates
```bash
cd docker/
./generate-certs.sh
```
This script:
- Builds the certificate manager Docker image
- Generates all required certificates with proper user ownership
- Ensures certificates are readable/writable by the current user
- Creates Java keystores and truststores
- Provides detailed certificate information

#### Clean Up Certificates
```bash
cd docker/
./cleanup-certs.sh
```
This script:
- Removes all existing certificates
- Fixes file permissions (requires sudo)
- Recreates clean certificate directory structure
- Allows normal users to manage certificates afterward

### Certificate Ownership
The generate-certs.sh script automatically sets proper ownership so that:
- Normal users can read, modify, and delete certificates
- No sudo access is required for certificate management
- Docker containers can still access certificates as needed

### Certificate Utilities
The application includes utilities for:
- Self-signed certificate generation
- Certificate validation and information display
- Keystore and truststore management
- Certificate import/export functionality

For detailed SSL configuration documentation, see the main project's `SSL_EXAMPLES.md` file.
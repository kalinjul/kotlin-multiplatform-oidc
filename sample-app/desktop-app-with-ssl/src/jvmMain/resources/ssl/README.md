# SSL Resources Directory

This directory contains SSL/TLS certificates and configuration files for the OIDC SSL sample application.

## Directory Structure

```
ssl/
├── certificates/         # Primary certificates loaded by the application
│   ├── localhost.p12     # PKCS12 certificate for localhost (password: localhost)
│   └── localhost.jks     # JKS certificate for localhost (password: localhost)
├── examples/             # Example certificates for reference
│   ├── client-cert.p12   # Sample client certificate (password: client123)
│   ├── custom-ca.crt     # Sample CA certificate
│   └── truststore.jks    # Sample truststore (password: truststore)
└── keytool-commands.txt  # Certificate generation commands
```

## Resource Certificate Loading

The application automatically detects and loads certificates from resources using this priority order:

1. `/ssl/certificates/{hostname}.p12`
2. `/ssl/certificates/{hostname}.jks`  
3. `/ssl/certificates/localhost.p12`
4. `/ssl/certificates/localhost.jks`

### Usage in Code
```kotlin
// Auto-detect resources certificate
val certificateSource = CertificateSourceFactory.autoDetect()

// Explicit resources certificate  
val certificateSource = CertificateSourceFactory.fromResources()

// Specific resource path
val certificateSource = CertificateSourceFactory.fromResources(
    resourcePath = "/ssl/certificates/localhost.p12",
    password = "localhost"
)
```

## Certificate Generation

To generate a self-signed certificate for localhost:

```bash
keytool -genkeypair \
    -alias localhost \
    -keyalg RSA \
    -keysize 2048 \
    -validity 365 \
    -keystore localhost.p12 \
    -storetype PKCS12 \
    -storepass localhost \
    -keypass localhost \
    -dname "CN=localhost, OU=Development, O=OIDC SSL Demo, L=Local, ST=Local, C=US" \
    -ext SAN=dns:localhost,ip:127.0.0.1
```

## Trust Store Setup

To create a custom trust store:

```bash
# Import a CA certificate
keytool -import \
    -alias custom-ca \
    -file custom-ca.crt \
    -keystore truststore.jks \
    -storepass truststore \
    -noprompt

# List certificates in trust store
keytool -list -keystore truststore.jks -storepass truststore
```

## Usage in Application

### Loading Certificates
```kotlin
val keystorePath = "/path/to/localhost.p12"
val keystorePassword = "localhost"
val truststorePath = "/path/to/truststore.jks" 
val truststorePassword = "truststore"
```

### SSL Configuration
```kotlin
ssl {
    keyStore(keystorePath, keystorePassword)
    trustStore(truststorePath, truststorePassword)
}
```

## Security Notes

- These are sample certificates for development and demonstration only
- Never use self-signed certificates in production
- Store certificate passwords securely
- Regularly rotate certificates and update trust stores
- Use proper CA-signed certificates for production deployments

## Browser Import

For testing HTTPS redirect flows, you may need to import the self-signed certificate into your browser:

1. **Chrome/Edge**: Settings → Privacy and Security → Security → Manage Certificates
2. **Firefox**: Settings → Privacy & Security → Certificates → View Certificates  
3. **Safari**: Keychain Access → Import certificate

After import, mark the certificate as trusted for web authentication.
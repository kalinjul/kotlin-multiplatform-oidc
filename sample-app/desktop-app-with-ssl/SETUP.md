# OIDC SSL Demo Environment - Setup Guide

This guide provides comprehensive instructions for setting up and running the SSL-enabled OIDC demonstration environment.

## Overview

The OIDC SSL Demo Environment provides a complete testing ground for SSL/TLS configurations with OAuth 2.0 and OpenID Connect flows. It includes:

- **Dockerized Keycloak** with SSL/TLS configuration and pre-configured realm
- **PostgreSQL** with SSL connections
- **Certificate Authority** and automated certificate management
- **Sample applications** demonstrating SSL scenarios
- **Ready-to-use OIDC clients** and demo users

## Quick Start

For immediate setup, use the quick start script:

```bash
./demo/quick_start.sh
```

This script will automatically:
1. Check prerequisites (Docker, Java)
2. Generate SSL certificates
3. Start the Docker environment (Keycloak + PostgreSQL)
4. Load pre-configured OIDC realm, clients, and users
5. Verify the environment is ready

## Manual Setup

### Prerequisites

#### Required Software
- **Docker** (20.10 or later)
- **Docker Compose** (v2 recommended)
- **Java 17+** (for sample application)

#### Optional Software
- **curl** (for testing)
- **openssl** (for certificate inspection)
- **keytool** (for Java keystore management)

### Step 1: Generate SSL Certificates

The environment requires a complete PKI infrastructure:

```bash
cd docker
docker compose --profile cert-generation up cert-manager
```

This generates:
- **Certificate Authority (CA)**: Root certificate for the demo
- **Server certificates**: For Keycloak, PostgreSQL, NGINX
- **Client certificates**: For mutual TLS testing
- **Java keystores**: JKS and PKCS12 formats

#### Certificate Details

| Component | Certificate | Location | Password |
|-----------|-------------|----------|----------|
| CA | ca.crt | `docker/certs/ca/` | N/A |
| Keycloak | keycloak.{crt,key} | `docker/certs/keycloak/` | keystore-password |
| PostgreSQL | server.{crt,key} | `docker/certs/postgres/` | N/A |
| Client (mTLS) | client.{crt,key} | `docker/certs/client/` | client-password |
| Sample App | localhost.{crt,key} | `docker/certs/localhost/` | localhost |

### Step 2: Start the Docker Environment

Start all services (dual-mode architecture):

```bash
cd docker
docker compose up -d postgres-https postgres-http keycloak keycloak-http
```

#### Service Ports

| Service | Port | Protocol | Description |
|---------|------|----------|-------------|
| PostgreSQL HTTPS | 7000 | TCP | Database for HTTPS Keycloak |
| PostgreSQL HTTP | 7005 | TCP | Database for HTTP Keycloak |
| Keycloak HTTPS | 7001 | HTTPS | Identity provider (SSL) |
| Keycloak HTTP | 7080 | HTTP | Identity provider (dev mode) |
| HTTPS Management | 7002 | HTTPS | Management interface |
| HTTP Management | 7003 | HTTP | Management interface |
| NGINX (optional) | 7004 | HTTPS | Reverse proxy |
| NGINX HTTP (optional) | 7005 | HTTP | HTTP redirect |

#### Health Checks

Wait for services to be ready:

```bash
# PostgreSQL HTTPS
docker compose exec postgres-https pg_isready -U keycloak

# PostgreSQL HTTP
docker compose exec postgres-http pg_isready -U keycloak_http

# Keycloak HTTPS
curl -f https://localhost:7001/realms/master/.well-known/openid-configuration --insecure

# Keycloak HTTP
curl -f http://localhost:7080/realms/master/.well-known/openid-configuration
```

### Step 3: Trust the CA Certificate

For browsers and applications to trust the SSL certificates:

#### Browser Trust (Chrome, Edge, Firefox)

1. Download CA certificate: `docker/certs/ca/ca.crt`
2. Import into browser certificate store
3. Mark as trusted for web authentication

#### System Trust (Linux)

```bash
sudo cp docker/certs/ca/ca.crt /usr/local/share/ca-certificates/oidc-ssl-demo-ca.crt
sudo update-ca-certificates
```

#### System Trust (macOS)

```bash
sudo security add-trusted-cert -d -r trustRoot -k /System/Library/Keychains/SystemRootCertificates.keychain docker/certs/ca/ca.crt
```

#### System Trust (Windows)

```powershell
# Run as Administrator
Import-Certificate -FilePath "docker\certs\ca\ca.crt" -CertStoreLocation Cert:\LocalMachine\Root
```

## Testing the Environment

### Manual Testing

#### Test Keycloak Connectivity

```bash
# Test HTTPS discovery endpoint
curl https://localhost:7001/realms/playground/.well-known/openid-configuration

# Test with CA verification
curl --cacert docker/certs/ca/ca.crt https://localhost:7001/realms/playground/.well-known/openid-configuration

# Test HTTP discovery endpoint
curl http://localhost:7080/realms/playground/.well-known/openid-configuration
```

#### Test OAuth Flow

**HTTPS Mode:**
1. Access Keycloak: https://localhost:7001/realms/playground/account/
2. Login with: `demo` / `demo`
3. Verify SSL certificate in browser

**HTTP Mode:**
1. Access Keycloak: http://localhost:7080/realms/playground/account/
2. Login with: `demo` / `demo`
3. No SSL certificate verification needed

### Sample Application

Build and run the SSL-enabled sample application:

```bash
cd ../..  # Navigate to sample-app root
./gradlew :desktop-app-with-ssl:run
```

The sample app demonstrates:
- HTTPS redirect server configuration
- HTTP client SSL settings
- Complete OAuth flows with SSL
- Certificate management

## SSL Configuration Scenarios

### 1. Basic SSL Client

**Use Case**: Standard HTTPS connections with system trust store

**Configuration**:
```kotlin
val client = OpenIdConnectClient(discoveryUri = "https://localhost:7001/realms/playground/.well-known/openid-configuration") {
    clientId = "basic-client"
    clientSecret = "basic-client-secret"
    redirectUri = "https://localhost:7080/redirect"
}
```

### 2. Custom Trust Store

**Use Case**: Corporate environments with custom CA

**Configuration**:
```kotlin
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "basic-client"
    ssl {
        trustStore("docker/certs/universal/truststore.jks", "truststore-password")
    }
}.createSslEnabledClient()
```

### 3. Mutual TLS (mTLS)

**Use Case**: High-security environments requiring client certificates

**Configuration**:
```kotlin
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "mtls-client"
    ssl {
        keyStore("docker/certs/client/client.p12", "client-password")
        trustStore("docker/certs/universal/truststore.jks", "truststore-password")
    }
}.createSslEnabledClient()
```

### 4. Development Mode

**Use Case**: Development with self-signed certificates

**Configuration**:
```kotlin
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "dev-client"
    ssl {
        disableCertificateValidation()
        disableHostnameVerification()
    }
}.createSslEnabledClient()
```

## Pre-configured Users

The environment comes with ready-to-use demo users:

| Username | Password | Email | Roles | Description |
|----------|----------|-------|-------|-------------|
| demo | demo | demo@example.com | user | Basic demo user for testing |
| ssluser | sslpassword | ssl@example.com | user | SSL-specific demo user |

### Adding New Users

Users can be added through the Keycloak Admin Console:

1. Access: https://localhost:7001/admin/
2. Login with: `admin` / `admin` 
3. Navigate to Users → Add User
4. Configure user details and credentials

## Troubleshooting

### Common Issues

#### 1. Certificate Trust Issues

**Problem**: Browser shows "Not Secure" warnings

**Solution**:
1. Verify CA certificate is imported correctly
2. Clear browser cache and cookies
3. Restart browser after certificate import

#### 2. Connection Refused

**Problem**: Cannot connect to Keycloak

**Solution**:
1. Check Docker containers are running: `docker compose ps`
2. Check service logs: `docker compose logs keycloak`
3. Verify ports are not blocked by firewall

#### 3. SSL Handshake Failures

**Problem**: SSL connection errors

**Solution**:
1. Verify certificate validity: `openssl x509 -in docker/certs/keycloak/keycloak.crt -text -noout`
2. Check certificate SAN includes correct hostnames
3. Verify certificate chain: `openssl verify -CAfile docker/certs/ca/ca.crt docker/certs/keycloak/keycloak.crt`

#### 4. Desktop Application Issues

**Problem**: Desktop app can't access certificates

**Solution**:
1. Run certificate fix script: `./docker/fix-cert-permissions.sh`
2. Verify certificate generation: Check `docker/certs/localhost/` directory
3. Try regenerating certificates: `docker compose --profile cert-generation up cert-manager`

### Debug Commands

#### View Service Status
```bash
docker compose ps
docker compose logs -f keycloak
docker compose logs -f postgres
```

#### Test SSL Connections
```bash
# Test Keycloak SSL
openssl s_client -connect localhost:7001 -servername localhost

# Test with CA verification
openssl s_client -connect localhost:7001 -CAfile docker/certs/ca/ca.crt

# Test client certificate
openssl s_client -connect localhost:7001 -cert docker/certs/client/client.crt -key docker/certs/client/client.key
```

#### Certificate Information
```bash
# View certificate details
openssl x509 -in docker/certs/keycloak/keycloak.crt -text -noout

# View certificate chain
openssl crl2pkcs7 -nocrl -certfile docker/certs/keycloak/keycloak.crt | openssl pkcs7 -print_certs -text -noout

# Check certificate expiration
openssl x509 -in docker/certs/keycloak/keycloak.crt -checkend 86400
```

## Maintenance

### Updating Certificates

Regenerate certificates:
```bash
cd docker
docker compose down
rm -rf certs/
docker compose --profile cert-generation up cert-manager
docker compose up -d
```

### Backup and Restore

#### Backup
```bash
# Backup data
docker compose exec postgres pg_dump -U keycloak keycloak > backup.sql

# Backup certificates
tar -czf certificates-backup.tar.gz docker/certs/
```

#### Restore
```bash
# Restore data
docker compose exec -T postgres psql -U keycloak keycloak < backup.sql

# Restore certificates
tar -xzf certificates-backup.tar.gz
```

### Reset Environment

Complete reset:
```bash
./demo/reset_environment.sh --full
```

Partial reset (keep certificates):
```bash
./demo/reset_environment.sh --remove-volumes
```

## Security Considerations

### Development vs Production

**Development (This Demo)**:
- Self-signed certificates
- Default passwords
- Relaxed SSL validation options
- All services on localhost

**Production Recommendations**:
- CA-signed certificates
- Strong, unique passwords
- Strict SSL validation
- Proper network segmentation
- Regular certificate rotation

### Best Practices

1. **Certificate Management**:
   - Use proper CA-signed certificates in production
   - Implement certificate rotation procedures
   - Monitor certificate expiration

2. **Password Security**:
   - Change all default passwords
   - Use strong, unique passwords
   - Implement proper secret management

3. **Network Security**:
   - Use HTTPS for all communications
   - Implement proper firewall rules
   - Use VPNs for remote access

4. **Monitoring**:
   - Monitor SSL certificate expiration
   - Log and audit authentication attempts
   - Monitor for SSL/TLS vulnerabilities

## Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OAuth 2.0 Specification](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Specification](https://openid.net/connect/)
- [SSL/TLS Best Practices](https://wiki.mozilla.org/Security/Server_Side_TLS)

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review Docker and Keycloak logs
3. Test with simplified configurations
4. Verify certificate validity and trust chain
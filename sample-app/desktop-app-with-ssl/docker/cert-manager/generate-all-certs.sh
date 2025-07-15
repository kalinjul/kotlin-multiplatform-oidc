#!/bin/bash

set -euo pipefail

# Certificate generation script for OIDC SSL demo environment
# This script creates a complete PKI infrastructure for the demo

CERT_DIR="/certs"
CA_DIR="${CERT_DIR}/ca"
KEYCLOAK_DIR="${CERT_DIR}/keycloak"
CLIENT_DIR="${CERT_DIR}/client"
POSTGRES_DIR="${CERT_DIR}/postgres"
NGINX_DIR="${CERT_DIR}/nginx"

# Certificate parameters
CA_DAYS=3650
SERVER_DAYS=365
CLIENT_DAYS=365

echo "🔐 Starting certificate generation for OIDC SSL demo environment..."

# Ensure all directories exist
mkdir -p "${CA_DIR}" "${KEYCLOAK_DIR}" "${CLIENT_DIR}" "${POSTGRES_DIR}" "${NGINX_DIR}"

echo "📁 Created certificate directories"

# 1. Generate CA (Certificate Authority)
echo "🏛️  Generating Certificate Authority..."

# CA private key
openssl genrsa -out "${CA_DIR}/ca.key" 4096

# CA certificate
openssl req -new -x509 -days ${CA_DAYS} -key "${CA_DIR}/ca.key" -out "${CA_DIR}/ca.crt" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Certificate Authority/CN=Demo CA"

echo "✅ CA certificate generated"

# 2. Generate Keycloak server certificate
echo "🔑 Generating Keycloak server certificate..."

# Create extensions file for Keycloak
cat > "${KEYCLOAK_DIR}/keycloak.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = keycloak
DNS.2 = localhost
DNS.3 = oidc-keycloak
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

# Keycloak private key
openssl genrsa -out "${KEYCLOAK_DIR}/keycloak.key" 2048

# Keycloak certificate signing request
openssl req -new -key "${KEYCLOAK_DIR}/keycloak.key" -out "${KEYCLOAK_DIR}/keycloak.csr" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Keycloak/CN=localhost"

# Sign Keycloak certificate with CA
openssl x509 -req -in "${KEYCLOAK_DIR}/keycloak.csr" -CA "${CA_DIR}/ca.crt" -CAkey "${CA_DIR}/ca.key" \
    -CAcreateserial -out "${KEYCLOAK_DIR}/keycloak.crt" -days ${SERVER_DAYS} \
    -extfile "${KEYCLOAK_DIR}/keycloak.ext"

echo "✅ Keycloak server certificate generated"

# 3. Generate client certificate for mutual TLS
echo "👤 Generating client certificate for mutual TLS..."

# Create extensions file for client
cat > "${CLIENT_DIR}/client.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth
EOF

# Client private key
openssl genrsa -out "${CLIENT_DIR}/client.key" 2048

# Client certificate signing request
openssl req -new -key "${CLIENT_DIR}/client.key" -out "${CLIENT_DIR}/client.csr" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Client/CN=mtls-client"

# Sign client certificate with CA
openssl x509 -req -in "${CLIENT_DIR}/client.csr" -CA "${CA_DIR}/ca.crt" -CAkey "${CA_DIR}/ca.key" \
    -CAcreateserial -out "${CLIENT_DIR}/client.crt" -days ${CLIENT_DAYS} \
    -extfile "${CLIENT_DIR}/client.ext"

echo "✅ Client certificate for mutual TLS generated"

# 4. Generate PostgreSQL certificate
echo "🐘 Generating PostgreSQL server certificate..."

# Create extensions file for PostgreSQL
cat > "${POSTGRES_DIR}/postgres.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = postgres
DNS.2 = localhost
DNS.3 = oidc-postgres
IP.1 = 127.0.0.1
EOF

# PostgreSQL private key
openssl genrsa -out "${POSTGRES_DIR}/server.key" 2048

# PostgreSQL certificate signing request
openssl req -new -key "${POSTGRES_DIR}/server.key" -out "${POSTGRES_DIR}/server.csr" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=PostgreSQL/CN=postgres"

# Sign PostgreSQL certificate with CA
openssl x509 -req -in "${POSTGRES_DIR}/server.csr" -CA "${CA_DIR}/ca.crt" -CAkey "${CA_DIR}/ca.key" \
    -CAcreateserial -out "${POSTGRES_DIR}/server.crt" -days ${SERVER_DAYS} \
    -extfile "${POSTGRES_DIR}/postgres.ext"

# Copy CA certificate for PostgreSQL
cp "${CA_DIR}/ca.crt" "${POSTGRES_DIR}/ca.crt"

echo "✅ PostgreSQL server certificate generated"

# 5. Generate NGINX certificate (for reverse proxy scenarios)
echo "🌐 Generating NGINX server certificate..."

# Create extensions file for NGINX
cat > "${NGINX_DIR}/nginx.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = nginx
DNS.2 = localhost
DNS.3 = oidc-nginx
IP.1 = 127.0.0.1
EOF

# NGINX private key
openssl genrsa -out "${NGINX_DIR}/nginx.key" 2048

# NGINX certificate signing request
openssl req -new -key "${NGINX_DIR}/nginx.key" -out "${NGINX_DIR}/nginx.csr" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=NGINX/CN=localhost"

# Sign NGINX certificate with CA
openssl x509 -req -in "${NGINX_DIR}/nginx.csr" -CA "${CA_DIR}/ca.crt" -CAkey "${CA_DIR}/ca.key" \
    -CAcreateserial -out "${NGINX_DIR}/nginx.crt" -days ${SERVER_DAYS} \
    -extfile "${NGINX_DIR}/nginx.ext"

echo "✅ NGINX server certificate generated"

# 6. Create Java keystores and truststores
echo "☕ Creating Java keystores and truststores..."

# Keycloak keystore (PKCS12 format)
openssl pkcs12 -export -in "${KEYCLOAK_DIR}/keycloak.crt" -inkey "${KEYCLOAK_DIR}/keycloak.key" \
    -out "${KEYCLOAK_DIR}/keystore.p12" -name keycloak -passout pass:keystore-password

# Convert to JKS format for Keycloak
keytool -importkeystore -deststorepass keystore-password -destkeypass keystore-password \
    -destkeystore "${KEYCLOAK_DIR}/keystore.jks" -srckeystore "${KEYCLOAK_DIR}/keystore.p12" \
    -srcstoretype PKCS12 -srcstorepass keystore-password -alias keycloak

# Keycloak truststore
keytool -import -trustcacerts -alias ca -file "${CA_DIR}/ca.crt" \
    -keystore "${KEYCLOAK_DIR}/truststore.jks" -storepass truststore-password -noprompt

# Client keystore (PKCS12 format)
openssl pkcs12 -export -in "${CLIENT_DIR}/client.crt" -inkey "${CLIENT_DIR}/client.key" \
    -out "${CLIENT_DIR}/client.p12" -name client -passout pass:client-password

# Create truststore for clients
keytool -import -trustcacerts -alias ca -file "${CA_DIR}/ca.crt" \
    -keystore "${CLIENT_DIR}/truststore.jks" -storepass truststore-password -noprompt

echo "✅ Java keystores and truststores created"

# 7. Create localhost certificate for sample app
echo "🏠 Generating localhost certificate for sample app..."

mkdir -p "${CERT_DIR}/localhost"

# Create extensions file for localhost
cat > "${CERT_DIR}/localhost/localhost.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

# Localhost private key
openssl genrsa -out "${CERT_DIR}/localhost/localhost.key" 2048

# Localhost certificate signing request
openssl req -new -key "${CERT_DIR}/localhost/localhost.key" -out "${CERT_DIR}/localhost/localhost.csr" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Sample App/CN=localhost"

# Sign localhost certificate with CA
openssl x509 -req -in "${CERT_DIR}/localhost/localhost.csr" -CA "${CA_DIR}/ca.crt" -CAkey "${CA_DIR}/ca.key" \
    -CAcreateserial -out "${CERT_DIR}/localhost/localhost.crt" -days ${SERVER_DAYS} \
    -extfile "${CERT_DIR}/localhost/localhost.ext"

# Create PKCS12 keystore for localhost
openssl pkcs12 -export -in "${CERT_DIR}/localhost/localhost.crt" -inkey "${CERT_DIR}/localhost/localhost.key" \
    -out "${CERT_DIR}/localhost/localhost.p12" -name localhost -passout pass:localhost

echo "✅ Localhost certificate for sample app generated"

# 8. Set proper permissions
echo "🔒 Setting certificate permissions..."

# Make certificates readable by all (they're public)
find "${CERT_DIR}" -name "*.crt" -exec chmod 644 {} \;
find "${CERT_DIR}" -name "*.pem" -exec chmod 644 {} \;

# Make private keys readable only by owner
find "${CERT_DIR}" -name "*.key" -exec chmod 600 {} \;

# Make keystores readable by group (for Docker containers)
find "${CERT_DIR}" -name "*.p12" -exec chmod 640 {} \;
find "${CERT_DIR}" -name "*.jks" -exec chmod 640 {} \;

# Set PostgreSQL permissions (user 999 in container)
chown -R 999:999 "${POSTGRES_DIR}" 2>/dev/null || true
chmod 644 "${POSTGRES_DIR}"/*.crt 2>/dev/null || true
chmod 600 "${POSTGRES_DIR}"/*.key 2>/dev/null || true

# Fix ownership for normal user access (if USER_ID and GROUP_ID are provided)
if [ -n "${USER_ID:-}" ] && [ -n "${GROUP_ID:-}" ]; then
    echo "🔧 Fixing ownership for user ${USER_ID}:${GROUP_ID}..."
    
    # Change ownership of all files except PostgreSQL (which needs special permissions)
    find "${CERT_DIR}" -not -path "${POSTGRES_DIR}/*" -exec chown "${USER_ID}:${GROUP_ID}" {} \; 2>/dev/null || true
    
    # Set directory permissions to allow user access
    find "${CERT_DIR}" -type d -not -path "${POSTGRES_DIR}" -exec chmod 755 {} \; 2>/dev/null || true
    
    echo "✅ Ownership fixed for normal user access"
else
    echo "ℹ️  No USER_ID/GROUP_ID provided - certificates will be owned by root"
    echo "   Use the cleanup script to fix permissions if needed"
fi

echo "✅ Certificate permissions set"

# 9. Generate certificate information summary
echo "📋 Generating certificate information summary..."

cat > "${CERT_DIR}/certificate-info.txt" <<EOF
OIDC SSL Demo Environment - Certificate Information
==================================================

Generated on: $(date)

Certificate Authority (CA):
  - File: ca/ca.crt
  - Subject: /C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Certificate Authority/CN=Demo CA
  - Validity: ${CA_DAYS} days

Keycloak Server Certificate:
  - Files: keycloak/keycloak.{crt,key}
  - Keystore: keycloak/keystore.{jks,p12} (password: keystore-password)
  - Truststore: keycloak/truststore.jks (password: truststore-password)
  - Subject: /C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Keycloak/CN=localhost
  - SAN: DNS:keycloak, DNS:localhost, DNS:oidc-keycloak, IP:127.0.0.1
  - Validity: ${SERVER_DAYS} days

Client Certificate (Mutual TLS):
  - Files: client/client.{crt,key}
  - Keystore: client/client.p12 (password: client-password)
  - Subject: /C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Client/CN=mtls-client
  - Validity: ${CLIENT_DAYS} days

PostgreSQL Server Certificate:
  - Files: postgres/server.{crt,key}
  - Subject: /C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=PostgreSQL/CN=postgres
  - SAN: DNS:postgres, DNS:localhost, DNS:oidc-postgres, IP:127.0.0.1
  - Validity: ${SERVER_DAYS} days

NGINX Server Certificate:
  - Files: nginx/nginx.{crt,key}
  - Subject: /C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=NGINX/CN=localhost
  - SAN: DNS:nginx, DNS:localhost, DNS:oidc-nginx, IP:127.0.0.1
  - Validity: ${SERVER_DAYS} days

Sample App Localhost Certificate:
  - Files: localhost/localhost.{crt,key}
  - Keystore: localhost/localhost.p12 (password: localhost)
  - Subject: /C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Sample App/CN=localhost
  - SAN: DNS:localhost, IP:127.0.0.1
  - Validity: ${SERVER_DAYS} days

Access URLs:
  - Keycloak: https://localhost:7001
  - PostgreSQL: localhost:7000 (SSL required)
  - NGINX: https://localhost:7003
  - Sample App: https://localhost:7080

Trust Store Import:
To trust these certificates in your browser or application, import the CA certificate:
  File: ca/ca.crt
  
For Java applications, import into cacerts or use the provided truststore.jks files.

Security Notes:
- These certificates are for development and demonstration only
- The CA private key is included - do not use in production
- All passwords are default values - change for any real deployment
- Certificates use RSA 2048-bit keys (4096 for CA)
EOF

echo "✅ Certificate information summary created"

# 10. Clean up temporary files
echo "🧹 Cleaning up temporary files..."

find "${CERT_DIR}" -name "*.csr" -delete
find "${CERT_DIR}" -name "*.ext" -delete
find "${CERT_DIR}" -name "*.srl" -delete

echo "✅ Temporary files cleaned up"

echo "🎉 Certificate generation completed successfully!"
echo ""
echo "📍 All certificates have been generated in: ${CERT_DIR}"
echo "📖 See certificate-info.txt for detailed information"
echo ""
echo "Next steps:"
echo "1. Start the Docker environment: docker-compose up -d"
echo "2. Import ca/ca.crt into your browser/system trust store"
echo "3. Access Keycloak at: https://localhost:7001"
echo "4. Run the sample application with SSL support"
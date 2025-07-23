#!/bin/bash

set -euo pipefail

# Create Java keystores and truststores for the OIDC SSL demo environment

echo "☕ Creating Java keystores and truststores..."

CA_DIR="/certs/ca"
KEYCLOAK_DIR="/certs/keycloak"
CLIENT_DIR="/certs/client"

# Check if CA exists
if [ ! -f "${CA_DIR}/ca.crt" ]; then
    echo "❌ CA certificate not found. Please run certificate generation first."
    exit 1
fi

# 1. Create Keycloak keystore if server certificate exists
if [ -f "${KEYCLOAK_DIR}/keycloak.crt" ] && [ -f "${KEYCLOAK_DIR}/keycloak.key" ]; then
    echo "🔑 Creating Keycloak keystore..."
    
    # Create PKCS12 keystore
    openssl pkcs12 -export -in "${KEYCLOAK_DIR}/keycloak.crt" -inkey "${KEYCLOAK_DIR}/keycloak.key" \
        -out "${KEYCLOAK_DIR}/keystore.p12" -name keycloak -passout pass:keystore-password
    
    # Convert to JKS format
    keytool -importkeystore -deststorepass keystore-password -destkeypass keystore-password \
        -destkeystore "${KEYCLOAK_DIR}/keystore.jks" -srckeystore "${KEYCLOAK_DIR}/keystore.p12" \
        -srcstoretype PKCS12 -srcstorepass keystore-password -alias keycloak -noprompt
    
    echo "✅ Keycloak keystore created"
else
    echo "⚠️  Keycloak certificate not found, skipping keystore creation"
fi

# 2. Create Keycloak truststore
echo "🛡️  Creating Keycloak truststore..."
keytool -import -trustcacerts -alias ca -file "${CA_DIR}/ca.crt" \
    -keystore "${KEYCLOAK_DIR}/truststore.jks" -storepass truststore-password -noprompt

echo "✅ Keycloak truststore created"

# 3. Create client truststore
echo "👥 Creating client truststore..."
keytool -import -trustcacerts -alias ca -file "${CA_DIR}/ca.crt" \
    -keystore "${CLIENT_DIR}/truststore.jks" -storepass truststore-password -noprompt

echo "✅ Client truststore created"

# 4. Create universal truststore for applications
echo "🌐 Creating universal truststore..."
UNIVERSAL_DIR="/certs/universal"
mkdir -p "${UNIVERSAL_DIR}"

keytool -import -trustcacerts -alias ca -file "${CA_DIR}/ca.crt" \
    -keystore "${UNIVERSAL_DIR}/truststore.jks" -storepass truststore-password -noprompt

# Import all server certificates into universal truststore
for cert_dir in keycloak postgres nginx localhost; do
    cert_file="/certs/${cert_dir}/server.crt"
    if [ ! -f "$cert_file" ]; then
        cert_file="/certs/${cert_dir}/${cert_dir}.crt"
    fi
    if [ ! -f "$cert_file" ] && [ "$cert_dir" = "localhost" ]; then
        cert_file="/certs/localhost/localhost.crt"
    fi
    
    if [ -f "$cert_file" ]; then
        echo "  Adding ${cert_dir} certificate to universal truststore..."
        keytool -import -trustcacerts -alias "${cert_dir}" -file "$cert_file" \
            -keystore "${UNIVERSAL_DIR}/truststore.jks" -storepass truststore-password -noprompt 2>/dev/null || true
    fi
done

echo "✅ Universal truststore created"

# 5. List keystore contents for verification
echo "📋 Keystore contents:"

if [ -f "${KEYCLOAK_DIR}/keystore.jks" ]; then
    echo "  Keycloak keystore:"
    keytool -list -keystore "${KEYCLOAK_DIR}/keystore.jks" -storepass keystore-password | grep "Alias name:"
fi

echo "  Keycloak truststore:"
keytool -list -keystore "${KEYCLOAK_DIR}/truststore.jks" -storepass truststore-password | grep "Alias name:"

echo "  Client truststore:"
keytool -list -keystore "${CLIENT_DIR}/truststore.jks" -storepass truststore-password | grep "Alias name:"

echo "  Universal truststore:"
keytool -list -keystore "${UNIVERSAL_DIR}/truststore.jks" -storepass truststore-password | grep "Alias name:"

echo "🎉 All keystores and truststores created successfully!"
echo ""
echo "Keystore locations:"
echo "  - Keycloak: ${KEYCLOAK_DIR}/keystore.{jks,p12} (password: keystore-password)"
echo "  - Keycloak Truststore: ${KEYCLOAK_DIR}/truststore.jks (password: truststore-password)"
echo "  - Client Truststore: ${CLIENT_DIR}/truststore.jks (password: truststore-password)"
echo "  - Universal Truststore: ${UNIVERSAL_DIR}/truststore.jks (password: truststore-password)"
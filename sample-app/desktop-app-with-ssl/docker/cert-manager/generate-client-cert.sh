#!/bin/bash

set -euo pipefail

# Usage: ./generate-client-cert.sh <client_name> <common_name>
# Example: ./generate-client-cert.sh demo-client "Demo Client"

if [ $# -lt 2 ]; then
    echo "Usage: $0 <client_name> <common_name>"
    echo "Example: $0 demo-client \"Demo Client\""
    exit 1
fi

CLIENT_NAME=$1
COMMON_NAME=$2

echo "👤 Generating client certificate for ${CLIENT_NAME}..."

CA_DIR="/certs/ca"
CERT_DIR="/certs/client"

# Ensure directories exist
mkdir -p "${CERT_DIR}"

# Check if CA exists
if [ ! -f "${CA_DIR}/ca.crt" ] || [ ! -f "${CA_DIR}/ca.key" ]; then
    echo "❌ CA certificate not found. Please run generate-ca.sh first."
    exit 1
fi

# Create extensions file for client authentication
cat > "${CERT_DIR}/${CLIENT_NAME}.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth
EOF

# Generate private key
openssl genrsa -out "${CERT_DIR}/${CLIENT_NAME}.key" 2048

# Generate certificate signing request
openssl req -new -key "${CERT_DIR}/${CLIENT_NAME}.key" -out "${CERT_DIR}/${CLIENT_NAME}.csr" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Client/CN=${COMMON_NAME}"

# Sign certificate with CA
openssl x509 -req -in "${CERT_DIR}/${CLIENT_NAME}.csr" -CA "${CA_DIR}/ca.crt" -CAkey "${CA_DIR}/ca.key" \
    -CAcreateserial -out "${CERT_DIR}/${CLIENT_NAME}.crt" -days 365 \
    -extfile "${CERT_DIR}/${CLIENT_NAME}.ext"

# Create PKCS12 keystore
openssl pkcs12 -export -in "${CERT_DIR}/${CLIENT_NAME}.crt" -inkey "${CERT_DIR}/${CLIENT_NAME}.key" \
    -out "${CERT_DIR}/${CLIENT_NAME}.p12" -name "${CLIENT_NAME}" -passout pass:client-password

# Cleanup
rm "${CERT_DIR}/${CLIENT_NAME}.csr" "${CERT_DIR}/${CLIENT_NAME}.ext"

echo "✅ Client certificate generated for ${CLIENT_NAME}:"
echo "   Private Key: ${CERT_DIR}/${CLIENT_NAME}.key"
echo "   Certificate: ${CERT_DIR}/${CLIENT_NAME}.crt"
echo "   PKCS12 Keystore: ${CERT_DIR}/${CLIENT_NAME}.p12 (password: client-password)"
echo "   Common Name: ${COMMON_NAME}"
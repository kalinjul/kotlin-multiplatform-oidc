#!/bin/bash

set -euo pipefail

# Usage: ./generate-server-cert.sh <service_name> <common_name> [additional_dns_names...]
# Example: ./generate-server-cert.sh keycloak localhost oidc-keycloak

if [ $# -lt 2 ]; then
    echo "Usage: $0 <service_name> <common_name> [additional_dns_names...]"
    echo "Example: $0 keycloak localhost oidc-keycloak"
    exit 1
fi

SERVICE_NAME=$1
COMMON_NAME=$2
shift 2
ADDITIONAL_DNS=("$@")

echo "🔑 Generating server certificate for ${SERVICE_NAME}..."

CA_DIR="/certs/ca"
CERT_DIR="/certs/${SERVICE_NAME}"

# Ensure directories exist
mkdir -p "${CERT_DIR}"

# Check if CA exists
if [ ! -f "${CA_DIR}/ca.crt" ] || [ ! -f "${CA_DIR}/ca.key" ]; then
    echo "❌ CA certificate not found. Please run generate-ca.sh first."
    exit 1
fi

# Create extensions file
cat > "${CERT_DIR}/server.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = ${COMMON_NAME}
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

# Add additional DNS names
dns_counter=2
for dns_name in "${ADDITIONAL_DNS[@]}"; do
    echo "DNS.${dns_counter} = ${dns_name}" >> "${CERT_DIR}/server.ext"
    ((dns_counter++))
done

# Generate private key
openssl genrsa -out "${CERT_DIR}/server.key" 2048

# Generate certificate signing request
openssl req -new -key "${CERT_DIR}/server.key" -out "${CERT_DIR}/server.csr" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=${SERVICE_NAME}/CN=${COMMON_NAME}"

# Sign certificate with CA
openssl x509 -req -in "${CERT_DIR}/server.csr" -CA "${CA_DIR}/ca.crt" -CAkey "${CA_DIR}/ca.key" \
    -CAcreateserial -out "${CERT_DIR}/server.crt" -days 365 \
    -extfile "${CERT_DIR}/server.ext"

# Cleanup
rm "${CERT_DIR}/server.csr" "${CERT_DIR}/server.ext"

echo "✅ Server certificate generated for ${SERVICE_NAME}:"
echo "   Private Key: ${CERT_DIR}/server.key"
echo "   Certificate: ${CERT_DIR}/server.crt"
echo "   Common Name: ${COMMON_NAME}"
echo "   Additional DNS names: ${ADDITIONAL_DNS[*]:-none}"
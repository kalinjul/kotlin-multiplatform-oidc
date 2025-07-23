#!/bin/bash

set -euo pipefail

echo "🏛️  Generating Certificate Authority for OIDC SSL Demo..."

CA_DIR="/certs/ca"
mkdir -p "${CA_DIR}"

# Generate CA private key
openssl genrsa -out "${CA_DIR}/ca.key" 4096

# Generate CA certificate
openssl req -new -x509 -days 3650 -key "${CA_DIR}/ca.key" -out "${CA_DIR}/ca.crt" \
    -subj "/C=US/ST=Demo/L=SSL/O=OIDC SSL Demo/OU=Certificate Authority/CN=Demo CA"

echo "✅ Certificate Authority generated:"
echo "   Private Key: ${CA_DIR}/ca.key"
echo "   Certificate: ${CA_DIR}/ca.crt"
echo "   Valid for: 10 years"
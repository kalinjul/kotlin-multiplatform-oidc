#!/bin/bash

set -euo pipefail

# Certificate generation wrapper script for OIDC SSL demo environment
# This script runs the certificate generation with proper user permissions

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERT_DIR="${SCRIPT_DIR}/certs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔐 OIDC SSL Demo Certificate Generation${NC}"
echo "========================================"

# Check if Docker is available
if ! command -v docker >/dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not installed or not in PATH${NC}"
    exit 1
fi

# Create certificate directory if it doesn't exist
mkdir -p "$CERT_DIR"

echo -e "${BLUE}🏗️  Building certificate manager Docker image...${NC}"

# Build the certificate manager image
if docker build -t oidc-cert-manager "${SCRIPT_DIR}/cert-manager/" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Certificate manager image built successfully${NC}"
else
    echo -e "${RED}❌ Failed to build certificate manager image${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}🔑 Generating certificates...${NC}"

# Get current user ID and group ID
USER_ID=$(id -u)
GROUP_ID=$(id -g)

# Run the certificate generation with user permissions
if docker run --rm \
    -v "${CERT_DIR}:/certs" \
    -e USER_ID="$USER_ID" \
    -e GROUP_ID="$GROUP_ID" \
    oidc-cert-manager \
    /generate-all-certs.sh; then
    
    echo ""
    echo -e "${GREEN}🎉 Certificate generation completed successfully!${NC}"
else
    echo -e "${RED}❌ Certificate generation failed${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}🔍 Verifying certificate generation...${NC}"

# Check if certificates were generated
if [ -f "${CERT_DIR}/ca/ca.crt" ] && [ -f "${CERT_DIR}/keycloak/keycloak.crt" ]; then
    echo -e "${GREEN}✅ Certificate verification successful${NC}"
    
    # Show certificate information
    echo ""
    echo -e "${BLUE}📋 Certificate Information:${NC}"
    echo "  • CA Certificate: ${CERT_DIR}/ca/ca.crt"
    echo "  • Keycloak Certificate: ${CERT_DIR}/keycloak/keycloak.crt"
    echo "  • Client Certificate: ${CERT_DIR}/client/client.crt"
    echo "  • PostgreSQL Certificate: ${CERT_DIR}/postgres/server.crt"
    echo "  • NGINX Certificate: ${CERT_DIR}/nginx/nginx.crt"
    echo "  • Localhost Certificate: ${CERT_DIR}/localhost/localhost.crt"
    echo "  • Detailed Info: ${CERT_DIR}/certificate-info.txt"
    
    # Check permissions
    echo ""
    echo -e "${BLUE}🔒 Certificate Permissions:${NC}"
    
    # Check if user can read certificates
    if [ -r "${CERT_DIR}/ca/ca.crt" ]; then
        echo -e "  ✅ Certificates are readable by current user"
    else
        echo -e "  ⚠️  Certificates may not be readable by current user"
    fi
    
    # Check if user can modify certificates
    if [ -w "${CERT_DIR}/ca/" ]; then
        echo -e "  ✅ Certificate directory is writable by current user"
    else
        echo -e "  ⚠️  Certificate directory may not be writable by current user"
        echo -e "     Run ./cleanup-certs.sh to fix permissions"
    fi
    
else
    echo -e "${RED}❌ Certificate verification failed - missing certificate files${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}📖 Next Steps:${NC}"
echo "  1. Start the Docker environment:"
echo "     docker-compose up -d"
echo ""
echo "  2. Import the CA certificate into your browser/system trust store:"
echo "     File: ${CERT_DIR}/ca/ca.crt"
echo ""
echo "  3. Access services:"
echo "     • Keycloak: https://localhost:7001"
echo "     • Admin Console: https://localhost:7001/admin (admin/admin)"
echo ""
echo "  4. Run the sample desktop application with SSL support"
echo ""
echo -e "${BLUE}💡 Tips:${NC}"
echo "  • Use ./cleanup-certs.sh to remove all certificates"
echo "  • See ${CERT_DIR}/certificate-info.txt for detailed information"
echo "  • All certificates are valid for 1 year (CA: 10 years)"
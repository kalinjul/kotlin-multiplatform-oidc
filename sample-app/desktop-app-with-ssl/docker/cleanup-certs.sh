#!/bin/bash

set -euo pipefail

# Certificate cleanup script for OIDC SSL demo environment
# This script removes all certificates and fixes permissions for normal user access

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERT_DIR="${SCRIPT_DIR}/certs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧹 OIDC SSL Demo Certificate Cleanup${NC}"
echo "=========================================="

# Check if certificate directory exists
if [ ! -d "$CERT_DIR" ]; then
    echo -e "${YELLOW}⚠️  Certificate directory does not exist: $CERT_DIR${NC}"
    echo "Nothing to clean up."
    exit 0
fi

# Check if there are any files to clean
if [ -z "$(find "$CERT_DIR" -type f 2>/dev/null)" ]; then
    echo -e "${GREEN}✅ No certificate files found. Directory is already clean.${NC}"
    exit 0
fi

echo -e "${YELLOW}📋 Found certificate files to clean up:${NC}"
find "$CERT_DIR" -type f -exec ls -la {} \; | head -10
if [ "$(find "$CERT_DIR" -type f | wc -l)" -gt 10 ]; then
    echo "... and $(expr $(find "$CERT_DIR" -type f | wc -l) - 10) more files"
fi

echo ""
echo -e "${BLUE}🔧 Fixing certificate permissions...${NC}"

# Fix permissions for all certificate files and directories
# This ensures normal users can delete these files
if command -v sudo >/dev/null 2>&1; then
    echo -e "${YELLOW}🔑 Using sudo to fix permissions...${NC}"
    
    # Fix directory permissions recursively
    sudo find "$CERT_DIR" -type d -exec chmod 755 {} \; 2>/dev/null || true
    
    # Fix file permissions recursively
    sudo find "$CERT_DIR" -type f -exec chmod 644 {} \; 2>/dev/null || true
    
    # Change ownership to current user
    sudo chown -R "$(id -u):$(id -g)" "$CERT_DIR" 2>/dev/null || true
    
    echo -e "${GREEN}✅ Permissions fixed${NC}"
else
    echo -e "${RED}❌ sudo not available. Trying to fix permissions without sudo...${NC}"
    
    # Try to fix permissions without sudo (might fail for some files)
    find "$CERT_DIR" -type d -exec chmod 755 {} \; 2>/dev/null || true
    find "$CERT_DIR" -type f -exec chmod 644 {} \; 2>/dev/null || true
    
    echo -e "${YELLOW}⚠️  Some files might still have restrictive permissions${NC}"
fi

echo ""
echo -e "${BLUE}🗑️  Removing all certificate files...${NC}"

# Remove all files and subdirectories
if rm -rf "$CERT_DIR"/* 2>/dev/null; then
    echo -e "${GREEN}✅ All certificate files removed successfully${NC}"
else
    echo -e "${RED}❌ Failed to remove some files. They might have restrictive permissions.${NC}"
    echo "Files that couldn't be removed:"
    find "$CERT_DIR" -type f 2>/dev/null || true
    exit 1
fi

echo ""
echo -e "${BLUE}📁 Recreating certificate directory structure...${NC}"

# Recreate the basic directory structure with proper permissions
mkdir -p "$CERT_DIR"/{ca,keycloak,client,postgres,nginx,localhost,universal}

# Set proper permissions on directories
chmod 755 "$CERT_DIR"
chmod 755 "$CERT_DIR"/{ca,keycloak,client,postgres,nginx,localhost,universal}

echo -e "${GREEN}✅ Certificate directory structure recreated${NC}"

echo ""
echo -e "${BLUE}🔍 Verifying cleanup...${NC}"

# Verify cleanup was successful
if [ -z "$(find "$CERT_DIR" -type f 2>/dev/null)" ]; then
    echo -e "${GREEN}✅ Cleanup successful! No certificate files remaining.${NC}"
else
    echo -e "${RED}❌ Cleanup incomplete. Some files remain:${NC}"
    find "$CERT_DIR" -type f -exec ls -la {} \;
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 Certificate cleanup completed successfully!${NC}"
echo ""
echo "📍 Certificate directories are now clean and ready for new certificate generation."
echo "📖 Next steps:"
echo "   1. Run certificate generation: docker-compose --profile cert-generation up cert-manager"
echo "   2. Or use the quick start script: ./demo/quick_start.sh"
echo ""
echo -e "${BLUE}💡 Note: All certificate directories now have proper permissions (755)${NC}"
echo "   Normal users can now create, modify, and delete certificates without sudo."
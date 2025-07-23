#!/bin/bash

# Fix certificate permissions for desktop app access
# This script makes Docker-generated certificates accessible to the current user

SCRIPT_DIR="$(dirname "$0")"
CERTS_DIR="$SCRIPT_DIR/certs"
USER_CERT_DIR="$HOME/.oidc-desktop-ssl"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Status tracking
SUCCESS_COUNT=0
TOTAL_COUNT=0

echo -e "${BLUE}🔧 Configuring certificates for desktop app...${NC}"

# Create user certificate directory
mkdir -p "$USER_CERT_DIR"

# Function to copy certificate with fallback methods
copy_certificate() {
    local src_file="$1"
    local dst_file="$2"
    local name="$3"
    
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    
    if [ ! -f "$src_file" ]; then
        echo -e "${YELLOW}⚠️ $name not found: $src_file${NC}"
        return 1
    fi
    
    echo -e "${BLUE}📁 Processing $name...${NC}"
    
    # Try copying with different methods
    if cp "$src_file" "$dst_file" 2>/dev/null; then
        chmod 644 "$dst_file"
        echo -e "${GREEN}✅ $name copied successfully${NC}"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        return 0
    elif [ -r "$src_file" ]; then
        # If we can read it directly, copy using cat
        if cat "$src_file" > "$dst_file" 2>/dev/null; then
            chmod 644 "$dst_file"
            echo -e "${GREEN}✅ $name copied via cat${NC}"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
            return 0
        fi
    fi
    
    # Try to fix permissions on the original file
    echo -e "${YELLOW}💡 Attempting to fix Docker certificate permissions...${NC}"
    if sudo chmod 644 "$src_file" 2>/dev/null; then
        echo -e "${GREEN}✅ Fixed Docker permissions for $name${NC}"
        # Try copying again
        if cp "$src_file" "$dst_file" 2>/dev/null; then
            chmod 644 "$dst_file"
            echo -e "${GREEN}✅ $name copied after permission fix${NC}"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
            return 0
        fi
    fi
    
    echo -e "${RED}❌ Failed to copy $name${NC}"
    echo -e "${YELLOW}💡 Run: sudo chmod 644 $src_file${NC}"
    return 1
}

# Copy localhost certificate (primary certificate for HTTPS redirect server)
copy_certificate "$CERTS_DIR/localhost/localhost.p12" "$USER_CERT_DIR/localhost.p12" "Localhost server certificate"

# Copy client certificate (for mutual TLS demos)
copy_certificate "$CERTS_DIR/client/client.p12" "$USER_CERT_DIR/client.p12" "Client certificate"

# Copy CA certificate (for trusting Keycloak HTTPS)
copy_certificate "$CERTS_DIR/ca/ca.crt" "$USER_CERT_DIR/ca.crt" "CA certificate"

# Copy client truststore (for SSL client configuration)
copy_certificate "$CERTS_DIR/client/truststore.jks" "$USER_CERT_DIR/truststore.jks" "Client truststore"

echo ""
echo -e "${BLUE}🔗 Creating universal truststore...${NC}"

# Create universal truststore for app compatibility
UNIVERSAL_TRUSTSTORE_DIR="$CERTS_DIR/universal"
UNIVERSAL_TRUSTSTORE_FILE="$UNIVERSAL_TRUSTSTORE_DIR/truststore.jks"
mkdir -p "$UNIVERSAL_TRUSTSTORE_DIR"

TOTAL_COUNT=$((TOTAL_COUNT + 1))

if [ -f "$CERTS_DIR/ca/ca.crt" ]; then
    echo -e "${BLUE}📁 Building universal truststore with all CA certificates...${NC}"
    
    # Create universal truststore with CA certificate
    if keytool -import -trustcacerts -alias ca -file "$CERTS_DIR/ca/ca.crt" \
        -keystore "$UNIVERSAL_TRUSTSTORE_FILE" -storepass truststore-password -noprompt 2>/dev/null; then
        
        # Add server certificates to the truststore
        for cert_name in keycloak localhost postgres nginx; do
            cert_file="$CERTS_DIR/$cert_name/$cert_name.crt"
            if [ -f "$cert_file" ]; then
                keytool -import -trustcacerts -alias "$cert_name" -file "$cert_file" \
                    -keystore "$UNIVERSAL_TRUSTSTORE_FILE" -storepass truststore-password -noprompt 2>/dev/null || true
            fi
        done
        
        echo -e "${GREEN}✅ Universal truststore created successfully${NC}"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        echo -e "${YELLOW}⚠️ Failed to create universal truststore${NC}"
    fi
else
    echo -e "${YELLOW}⚠️ CA certificate not found - cannot create universal truststore${NC}"
fi

echo ""
echo -e "${BLUE}📋 Certificate Configuration Summary:${NC}"
echo -e "${GREEN}✅ Successfully configured: $SUCCESS_COUNT/$TOTAL_COUNT certificates${NC}"
echo ""
echo -e "${BLUE}🎯 Desktop app will now use (in priority order):${NC}"
echo "   1. User-copied certificates from ~/.oidc-desktop-ssl/"
echo "   2. Docker-generated certificates (if permissions allow)"
echo "   3. Resources certificates (bundled with app)"
echo "   4. Auto-generated certificates (fallback)"
echo ""
echo -e "${BLUE}📁 Certificate locations:${NC}"
echo "   • User certificates: $USER_CERT_DIR"
echo "   • Docker certificates: $CERTS_DIR"
echo "   • Universal truststore: $UNIVERSAL_TRUSTSTORE_DIR/truststore.jks"
echo ""

if [ $SUCCESS_COUNT -eq $TOTAL_COUNT ]; then
    echo -e "${GREEN}🎉 All certificates configured successfully! Desktop app is ready.${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠️ Some certificates couldn't be configured. Desktop app will use fallback mechanisms.${NC}"
    exit 1
fi
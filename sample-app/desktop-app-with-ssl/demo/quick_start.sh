#!/bin/bash

set -euo pipefail

# OIDC SSL Demo Environment - Quick Start Script
# This script sets up the complete SSL-enabled OIDC demo environment

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
DOCKER_DIR="$PROJECT_DIR/docker"
SCRIPTS_DIR="$PROJECT_DIR/scripts"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${PURPLE}$1${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    print_header "🔍 Checking prerequisites..."
    
    local all_good=true
    
    # Check Docker
    if command_exists docker; then
        print_success "Docker is installed"
        
        # Check if Docker daemon is running
        if docker ps >/dev/null 2>&1; then
            print_success "Docker daemon is running"
        else
            print_error "Docker daemon is not running. Please start Docker."
            all_good=false
        fi
    else
        print_error "Docker is not installed. Please install Docker first."
        all_good=false
    fi
    
    # Check Docker Compose
    if docker compose version >/dev/null 2>&1; then
        print_success "Docker Compose v2 is available"
    elif command_exists docker-compose; then
        print_success "Docker Compose v1 is available"
    else
        print_error "Docker Compose is not installed. Please install Docker Compose."
        all_good=false
    fi
    
    # Check Java (for sample app, optional)
    if command_exists java; then
        java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_success "Java is installed (version: $java_version) - good for sample app"
    else
        print_warning "Java is not installed. You'll need it to run the sample application."
        print_status "The SSL demo environment will still work without Java."
    fi
    
    if [ "$all_good" = false ]; then
        print_error "Some prerequisites are missing. Please install them and try again."
        exit 1
    fi
    
    print_success "All prerequisites check passed!"
}

# Function to generate certificates
generate_certificates() {
    print_header "🔐 Generating SSL certificates..."
    
    cd "$DOCKER_DIR"
    
    # Check if certificates already exist
    if [ -f "certs/ca/ca.crt" ]; then
        print_warning "Certificates already exist. Skipping generation."
        read -p "Do you want to regenerate certificates? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_status "Using existing certificates."
            return 0
        fi
        print_status "Regenerating certificates..."
        rm -rf certs/*
    fi
    
    # Use Docker Compose to generate certificates
    if docker compose --profile cert-generation up cert-manager; then
        print_success "Certificates generated successfully"
    else
        print_error "Failed to generate certificates"
        exit 1
    fi
}

# Function to start the environment
start_environment() {
    print_header "🚀 Starting OIDC SSL demo environment..."
    
    cd "$DOCKER_DIR"
    
    # Start all services: both databases + both Keycloak instances
    if docker compose up -d postgres-https postgres-http keycloak keycloak-http; then
        print_success "Docker services started"
    else
        print_error "Failed to start Docker services"
        exit 1
    fi
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    
    # Wait for both PostgreSQL instances
    wait_for_databases
    
    # Wait for both Keycloak instances
    wait_for_keycloak_services
}

# Function to wait for both databases
wait_for_databases() {
    print_status "Waiting for databases to be ready..."
    
    # Wait for HTTPS PostgreSQL
    print_status "Waiting for HTTPS PostgreSQL..."
    for i in {1..30}; do
        if docker compose exec postgres-https pg_isready -U keycloak >/dev/null 2>&1; then
            print_success "HTTPS PostgreSQL is ready"
            break
        fi
        sleep 2
        if [ $i -eq 30 ]; then
            print_error "HTTPS PostgreSQL failed to start"
            exit 1
        fi
    done
    
    # Wait for HTTP PostgreSQL
    print_status "Waiting for HTTP PostgreSQL..."
    for i in {1..30}; do
        if docker compose exec postgres-http pg_isready -U keycloak_http >/dev/null 2>&1; then
            print_success "HTTP PostgreSQL is ready"
            break
        fi
        sleep 2
        if [ $i -eq 30 ]; then
            print_error "HTTP PostgreSQL failed to start"
            exit 1
        fi
    done
}

# Function to wait for both Keycloak services
wait_for_keycloak_services() {
    print_status "Waiting for Keycloak services to be ready..."
    
    # Wait for HTTPS Keycloak
    print_status "Waiting for HTTPS Keycloak..."
    for i in {1..60}; do
        if curl -f https://localhost:7001/health --insecure --silent --max-time 5 >/dev/null 2>&1; then
            print_success "HTTPS Keycloak is ready"
            break
        fi
        sleep 3
        if [ $i -eq 60 ]; then
            print_error "HTTPS Keycloak failed to start"
            exit 1
        fi
    done
    
    # Wait for HTTP Keycloak
    print_status "Waiting for HTTP Keycloak..."
    for i in {1..60}; do
        if curl -f http://localhost:7080/health --silent --max-time 5 >/dev/null 2>&1; then
            print_success "HTTP Keycloak is ready"
            break
        fi
        sleep 3
        if [ $i -eq 60 ]; then
            print_error "HTTP Keycloak failed to start"
            exit 1
        fi
    done
}

# Function to wait for Keycloak to be ready
wait_for_keycloak() {
    print_header "⏳ Waiting for Keycloak to be ready..."
    
    cd "$DOCKER_DIR"
    
    local max_attempts=30
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -f https://localhost:7001/health --insecure --silent --max-time 5 > /dev/null 2>&1; then
            print_success "Keycloak is ready!"
            return 0
        fi
        
        attempt=$((attempt + 1))
        if [ $((attempt % 5)) -eq 0 ]; then
            print_status "Attempt $attempt/$max_attempts - Still waiting for Keycloak..."
        fi
        sleep 5
    done
    
    print_error "Keycloak failed to become ready after $max_attempts attempts"
    return 1
}

# Function to fix certificate permissions for desktop app
fix_certificate_permissions() {
    print_header "🔧 Configuring certificates for desktop app..."
    
    cd "$DOCKER_DIR"
    
    # Run the certificate permissions script
    if [ -f "fix-cert-permissions.sh" ]; then
        print_status "Making certificates accessible to desktop application..."
        if bash fix-cert-permissions.sh; then
            print_success "Desktop app certificates configured successfully"
        else
            print_warning "Certificate configuration completed with warnings - check output above"
        fi
    else
        print_warning "Certificate permissions script not found"
        print_status "Desktop app will use fallback certificate mechanisms"
    fi
}

# Function to validate certificate setup
validate_certificate_setup() {
    local cert_status="✅"
    local user_cert_dir="$HOME/.oidc-desktop-ssl"
    
    # Check Docker certificates
    if [ -f "$DOCKER_DIR/certs/ca/ca.crt" ]; then
        echo "   ✅ CA certificate: Available"
    else
        echo "   ❌ CA certificate: Missing"
        cert_status="❌"
    fi
    
    if [ -f "$DOCKER_DIR/certs/localhost/localhost.p12" ]; then
        echo "   ✅ Localhost certificate: Available"
    else
        echo "   ❌ Localhost certificate: Missing"
        cert_status="❌"
    fi
    
    if [ -f "$DOCKER_DIR/certs/universal/truststore.jks" ]; then
        echo "   ✅ Universal truststore: Available"
    else
        echo "   ⚠️ Universal truststore: Will be created on first run"
    fi
    
    # Check user certificates
    if [ -d "$user_cert_dir" ] && [ -f "$user_cert_dir/localhost.p12" ]; then
        echo "   ✅ User certificates: Configured"
    else
        echo "   ⚠️ User certificates: Will be configured on first run"
    fi
    
    # Overall status
    if [ "$cert_status" = "✅" ]; then
        echo "   🎉 Certificate setup: Ready for optimal SSL performance"
    else
        echo "   ⚠️ Certificate setup: Some certificates missing - regenerate if needed"
    fi
}

# Function to display summary
display_summary() {
    print_header "🎉 OIDC SSL Dual-Mode Environment Ready!"
    
    echo
    echo "📍 HTTPS Keycloak (Production-like):"
    echo "   🔐 Admin Console: https://localhost:7001/admin/"
    echo "   🎯 Realm: https://localhost:7001/realms/playground"
    echo "   🔍 Discovery: https://localhost:7001/realms/playground/.well-known/openid-configuration"
    echo "   🗄️ Database: localhost:7000"
    echo
    echo "📍 HTTP Keycloak (Development):"
    echo "   🔓 Admin Console: http://localhost:7080/admin/"
    echo "   🎯 Realm: http://localhost:7080/realms/playground"
    echo "   🔍 Discovery: http://localhost:7080/realms/playground/.well-known/openid-configuration"
    echo "   🗄️ Database: localhost:7005"
    echo
    
    # Certificate status validation
    echo "🔒 Certificate Status:"
    validate_certificate_setup
    echo
    echo "🔑 Default Credentials:"
    echo "   Keycloak Admin: admin / admin"
    echo "   Demo User: demo / demo"
    echo "   SSL User: ssluser / sslpassword"
    echo
    echo "💡 Usage Tips:"
    echo "   • Use HTTPS mode for production-like testing"
    echo "   • Use HTTP mode for development and debugging"
    echo "   • Both services share the same realm configuration"
    echo "   • Each service has independent database and state"
    echo
    echo "📋 Next Steps:"
    echo "   1. Import CA certificate for browser trust:"
    echo "      File: $PROJECT_DIR/docker/certs/ca/ca.crt"
    echo "      This enables browser access to https://localhost:7001"
    echo
    echo "   2. Run the sample application:"
    echo "      cd $PROJECT_DIR/../.."
    echo "      ./gradlew :desktop-app-with-ssl:run"
    echo "      (Desktop app certificates are automatically configured)"
    echo
    echo "   3. Available SSL demo scenarios:"
    echo "      • Basic SSL Client (basic-client/basic-client-secret)"
    echo "      • Mutual TLS Demo (mtls-client)" 
    echo "      • Development Mode (dev-client/dev-client-secret)"
    echo
    echo "   4. Demo users ready for testing:"
    echo "      • demo / demo"
    echo "      • ssluser / sslpassword"
    echo
    echo "   5. SSL Certificate troubleshooting:"
    echo "      • If desktop app has certificate issues:"
    echo "        cd $PROJECT_DIR/docker && bash fix-cert-permissions.sh"
    echo "      • If browser shows security warnings:"
    echo "        Import $PROJECT_DIR/docker/certs/ca/ca.crt as trusted CA"
    echo "      • Certificate locations:"
    echo "        Docker: $PROJECT_DIR/docker/certs/"
    echo "        User: ~/.oidc-desktop-ssl/"
    echo
    echo "🛠️  Management Commands:"
    echo "   Stop environment: cd docker && docker compose down"
    echo "   View HTTPS logs: cd docker && docker compose logs -f keycloak"
    echo "   View HTTP logs: cd docker && docker compose logs -f keycloak-http"
    echo "   Restart HTTPS: cd docker && docker compose restart keycloak"
    echo "   Restart HTTP: cd docker && docker compose restart keycloak-http"
    echo
    echo "📚 Documentation:"
    echo "   Setup Guide: $PROJECT_DIR/SETUP.md"
    echo "   SSL Examples: $PROJECT_DIR/../../../SSL_EXAMPLES.md"
    echo "   Troubleshooting: Check docker logs if issues occur"
}

# Function to cleanup on error
cleanup_on_error() {
    print_error "Setup failed. Cleaning up..."
    cd "$DOCKER_DIR"
    docker compose down
    exit 1
}

# Trap errors and cleanup
trap cleanup_on_error ERR

# Main execution
main() {
    print_header "🚀 OIDC SSL Demo Environment - Quick Start"
    print_header "=========================================="
    echo
    
    # Check if user wants to proceed
    echo "This script will:"
    echo "  • Generate SSL certificates"
    echo "  • Start Keycloak and PostgreSQL with Docker"
    echo "  • Load pre-configured OIDC realm and clients"
    echo "  • Provide ready-to-use SSL demo scenarios"
    echo "  • No manual configuration required!"
    echo
    read -p "Do you want to continue? (Y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Nn]$ ]]; then
        print_status "Setup cancelled by user."
        exit 0
    fi
    
    # Run setup steps
    check_prerequisites
    generate_certificates
    start_environment
    wait_for_keycloak
    fix_certificate_permissions
    display_summary
    
    print_success "🎉 Setup completed successfully!"
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
#!/bin/bash

set -euo pipefail

# OIDC SSL Demo Environment - Reset Script
# This script resets the demo environment to a clean state

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
DOCKER_DIR="$PROJECT_DIR/docker"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

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

# Function to stop and remove Docker containers
stop_containers() {
    print_header "🛑 Stopping Docker containers..."
    
    cd "$DOCKER_DIR"
    
    if docker compose ps -q | grep -q .; then
        print_status "Stopping Docker containers..."
        docker compose down
        
        # Remove volumes if requested
        if [ "${REMOVE_VOLUMES:-false}" = "true" ]; then
            print_status "Removing Docker volumes..."
            docker compose down -v
        fi
        
        print_success "Docker containers stopped"
    else
        print_status "No Docker containers are running"
    fi
}

# Function to remove certificates
remove_certificates() {
    print_header "🗑️  Removing SSL certificates..."
    
    if [ -d "$DOCKER_DIR/certs" ]; then
        if [ "${REMOVE_CERTS:-false}" = "true" ]; then
            print_status "Removing all certificates..."
            rm -rf "$DOCKER_DIR/certs"
            print_success "Certificates removed"
        else
            print_status "Keeping certificates (use --remove-certs to delete)"
        fi
    else
        print_status "No certificates found"
    fi
}


# Function to remove Docker images
remove_images() {
    print_header "🗑️  Removing Docker images..."
    
    if [ "${REMOVE_IMAGES:-false}" = "true" ]; then
        print_status "Removing custom Docker images..."
        
        # Remove custom built images
        docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}" | grep -E "(oidc-|cert-manager)" | awk '{print $3}' | xargs -r docker rmi -f
        
        # Optionally remove base images
        if [ "${REMOVE_BASE_IMAGES:-false}" = "true" ]; then
            print_status "Removing base Docker images..."
            docker rmi -f quay.io/keycloak/keycloak:23.0 postgres:15-alpine alpine:latest 2>/dev/null || true
        fi
        
        print_success "Docker images removed"
    else
        print_status "Keeping Docker images (use --remove-images to delete)"
    fi
}

# Function to display cleanup summary
display_summary() {
    print_header "🧹 Cleanup Summary"
    echo
    echo "The following actions were performed:"
    echo "  ✅ Docker containers stopped"
    
    if [ "${REMOVE_VOLUMES:-false}" = "true" ]; then
        echo "  ✅ Docker volumes removed"
    else
        echo "  ⏭️  Docker volumes kept"
    fi
    
    if [ "${REMOVE_CERTS:-false}" = "true" ]; then
        echo "  ✅ SSL certificates removed"
    else
        echo "  ⏭️  SSL certificates kept"
    fi
    
    
    if [ "${REMOVE_IMAGES:-false}" = "true" ]; then
        echo "  ✅ Docker images removed"
    else
        echo "  ⏭️  Docker images kept"
    fi
    
    echo
    echo "📋 To restart the environment:"
    echo "   ./quick_start.sh"
    echo
    echo "📋 To perform a complete cleanup:"
    echo "   ./reset_environment.sh --full"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Reset the OIDC SSL demo environment to a clean state."
    echo
    echo "Options:"
    echo "  --full                 Perform complete cleanup (remove everything)"
    echo "  --remove-volumes       Remove Docker volumes (data will be lost)"
    echo "  --remove-certs         Remove SSL certificates"
    echo "  --remove-images        Remove custom Docker images"
    echo "  --remove-base-images   Also remove base Docker images"
    echo "  --help, -h             Show this help message"
    echo
    echo "Examples:"
    echo "  $0                     Basic reset (stop containers only)"
    echo "  $0 --remove-volumes    Reset and remove data"
    echo "  $0 --full              Complete cleanup"
}

# Parse command line arguments
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --full)
                REMOVE_VOLUMES=true
                REMOVE_CERTS=true
                REMOVE_IMAGES=true
                shift
                ;;
            --remove-volumes)
                REMOVE_VOLUMES=true
                shift
                ;;
            --remove-certs)
                REMOVE_CERTS=true
                shift
                ;;
            --remove-images)
                REMOVE_IMAGES=true
                shift
                ;;
            --remove-base-images)
                REMOVE_BASE_IMAGES=true
                shift
                ;;
            --help|-h)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
}

# Main execution
main() {
    print_header "🧹 OIDC SSL Demo Environment - Reset"
    print_header "====================================="
    echo
    
    # Show what will be done
    echo "This script will:"
    echo "  • Stop all Docker containers"
    
    if [ "${REMOVE_VOLUMES:-false}" = "true" ]; then
        echo "  • Remove Docker volumes (⚠️  DATA WILL BE LOST)"
    fi
    
    if [ "${REMOVE_CERTS:-false}" = "true" ]; then
        echo "  • Remove SSL certificates"
    fi
    
    
    if [ "${REMOVE_IMAGES:-false}" = "true" ]; then
        echo "  • Remove Docker images"
    fi
    
    echo
    
    # Confirm with user
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_status "Reset cancelled by user."
        exit 0
    fi
    
    # Perform cleanup steps
    stop_containers
    remove_certificates
    remove_images
    display_summary
    
    print_success "🎉 Environment reset completed!"
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    parse_arguments "$@"
    main
fi
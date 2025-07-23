FROM alpine:latest

# Install required tools for certificate generation
RUN apk add --no-cache \
    openssl \
    openjdk11-jre-headless \
    bash \
    curl

# Create directories for certificates
RUN mkdir -p /certs/{ca,keycloak,client,postgres,nginx}

# Copy certificate generation scripts
COPY generate-all-certs.sh /generate-all-certs.sh
COPY generate-ca.sh /generate-ca.sh
COPY generate-server-cert.sh /generate-server-cert.sh
COPY generate-client-cert.sh /generate-client-cert.sh
COPY create-keystores.sh /create-keystores.sh

# Make scripts executable
RUN chmod +x /*.sh

# Set working directory
WORKDIR /certs

# Default command
CMD ["/generate-all-certs.sh"]
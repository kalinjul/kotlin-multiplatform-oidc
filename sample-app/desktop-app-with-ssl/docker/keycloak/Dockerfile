FROM quay.io/keycloak/keycloak:23.0

# Note: Using base Keycloak image tools (curl should be available)
USER root

# Copy SSL configuration files
COPY ssl-config/ /opt/keycloak/conf/ssl-config/

# Copy custom themes if any
COPY themes/ /opt/keycloak/themes/

# Create directories with proper permissions
RUN mkdir -p /opt/keycloak/conf/ssl && \
    mkdir -p /opt/keycloak/data/import && \
    chown -R keycloak:keycloak /opt/keycloak/conf/ssl && \
    chown -R keycloak:keycloak /opt/keycloak/data/import && \
    chmod 750 /opt/keycloak/conf/ssl && \
    chmod 755 /opt/keycloak/data/import

# Copy realm configuration
COPY realm-config.json /opt/keycloak/data/import/

# Switch back to keycloak user
USER keycloak

# Configure Keycloak for SSL and optimization
RUN /opt/keycloak/bin/kc.sh build \
    --db=postgres \
    --features=authorization,account2,admin-fine-grained-authz \
    --health-enabled=true \
    --metrics-enabled=true

# Set working directory
WORKDIR /opt/keycloak

# Expose ports
EXPOSE 8443 9000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f https://localhost:8443/health --insecure || exit 1

# Default command
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
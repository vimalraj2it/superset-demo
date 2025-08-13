# superset_config.py
import os

# Your App's Secret Key
SECRET_KEY = os.environ.get("SUPERSET_SECRET_KEY", "your_super_secret_key_for_superset")

# Configuration for embedded dashboards and guest token authentication
FEATURE_FLAGS = {
    "EMBEDDED_SUPERSET": True,
}

# The following secret is for signing guest tokens.
# Make sure it's long, complex, and stored securely.
GUEST_TOKEN_JWT_SECRET = os.environ.get("SUPERSET_GUEST_TOKEN_JWT_SECRET", "your-guest-token-secret-key-that-is-very-secret")

# Allow the frontend application to embed dashboards
TALISMAN_CONFIG = {
    "content_security_policy": {
        "frame-ancestors": ["'self'", "http://localhost:3000"],
    },
    "force_https": False,
    "strict_transport_security": False,
}

# This is needed to run behind a reverse proxy
ENABLE_PROXY_FIX = True
# Define the role name for guest users in Superset.
# This role needs to be created manually in the Superset UI
# and given access to the specific datasources and dashboards.
GUEST_ROLE_NAME = "Guest"

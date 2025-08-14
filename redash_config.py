# Redash Configuration
# This file overrides default Redash settings

# Disable frame-ancestors CSP restriction
FRAME_ANCESTORS = "'self' http://localhost:3000 http://localhost:8080 http://127.0.0.1:3000 http://127.0.0.1:8080"

# Allow iframe embedding
ALLOW_IFRAME_EMBEDDING = True

# Disable frame-ancestors CSP
DISABLE_FRAME_ANCESTORS = True

# Completely disable CSP for development
CONTENT_SECURITY_POLICY = None

# Disable all security headers that might interfere
SECURITY_HEADERS = {}

# Additional security settings
SECURE_COOKIES = False  # For local development

# Force disable frame-ancestors
def disable_frame_ancestors():
    return False

# Override the default CSP function
def get_content_security_policy():
    return None

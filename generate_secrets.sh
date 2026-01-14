#!/bin/bash
set -e

# Define paths
KEYSTORE_SRC="app/keystore.jks"
KEYSTORE_DST="app/keystore.b64"

GOOGLE_JSON_SRC="app/google-services.json"
GOOGLE_JSON_DST="app/google-services.json.b64"

# Helper function for cross-platform base64 encoding
encode_base64() {
    local src="$1"
    local dst="$2"
    
    # Restrict permissions on output file (CodeRabbit security fix)
    umask 077
    
    # Use -w 0 for no line wrapping (portable for secrets); fallback for macOS
    if base64 --help 2>&1 | grep -q '\-w'; then
        base64 -w 0 "$src" > "$dst"
    else
        base64 "$src" | tr -d '\n' > "$dst"
    fi
}

echo "🔐 Generating Base64 Secrets..."
echo "================================="

# 1. Handle Keystore
if [ -f "$KEYSTORE_SRC" ]; then
    encode_base64 "$KEYSTORE_SRC" "$KEYSTORE_DST"
    echo "✅ Generated: $KEYSTORE_DST"
else
    echo "⚠️  Skipped: $KEYSTORE_SRC not found."
fi

# 2. Handle Google Services JSON
if [ -f "$GOOGLE_JSON_SRC" ]; then
    encode_base64 "$GOOGLE_JSON_SRC" "$GOOGLE_JSON_DST"
    echo "✅ Generated: $GOOGLE_JSON_DST"
else
    echo "⚠️  Skipped: $GOOGLE_JSON_SRC not found."
fi

# 3. Automatic Upload via GitHub CLI
if command -v gh &> /dev/null; then
    echo ""
    echo "🚀 GitHub CLI (gh) detected. Attempting to upload code secrets..."
    
    # Check auth status first (quietly)
    if gh auth status &> /dev/null; then
        # Set KEYSTORE_B64
        if [ -f "$KEYSTORE_DST" ]; then
            gh secret set KEYSTORE_B64 < "$KEYSTORE_DST"
            echo "✅ Uploaded KEYSTORE_B64 to GitHub Secrets."
        fi

        # Set GOOGLE_SERVICES_JSON_B64
        if [ -f "$GOOGLE_JSON_DST" ]; then
            gh secret set GOOGLE_SERVICES_JSON_B64 < "$GOOGLE_JSON_DST"
            echo "✅ Uploaded GOOGLE_SERVICES_JSON_B64 to GitHub Secrets."
        fi
    else
        echo "⚠️  GitHub CLI is installed but not logged in. Run 'gh auth login' to enable auto-upload."
    fi
else
    echo ""
    echo "ℹ️  GitHub CLI (gh) not found. Skipping auto-upload."
fi

echo "================================="
echo "Instructions (if upload failed):"
echo "1. Open '$KEYSTORE_DST' -> Copy content -> GitHub Secret 'KEYSTORE_B64'"
echo "2. Open '$GOOGLE_JSON_DST' -> Copy content -> GitHub Secret 'GOOGLE_SERVICES_JSON_B64'"

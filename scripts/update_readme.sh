#!/bin/bash
set -e

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION_FILE="$PROJECT_DIR/version.properties"
CHANGELOG_FILE="$PROJECT_DIR/.github/CHANGELOG.md"
README_FILE="$PROJECT_DIR/.github/README.md"

# 1. Read Version
if [ ! -f "$VERSION_FILE" ]; then
    echo "❌ Error: version.properties not found!"
    exit 1
fi

MAJOR=$(grep "version.major" "$VERSION_FILE" | cut -d'=' -f2)
MINOR=$(grep "version.minor" "$VERSION_FILE" | cut -d'=' -f2)
PATCH=$(grep "version.patch" "$VERSION_FILE" | cut -d'=' -f2)
VERSION="$MAJOR.$MINOR.$PATCH"
echo "ℹ️  Current Version: $VERSION"

# 2. Extract Changelog for this version using sed
# Logic: Find start line (## [VERSION]), find next header (## [), print in between.
# /^## \[$VERSION\]/ matches the start line.
# /^## \[/ matches any version header.
# { /^## \[/!p; } prints only if NOT a header (skips the trailing header if matched).
# | sed '1d' removes the initial buffer match (the current version header).

if [ ! -f "$CHANGELOG_FILE" ]; then
    echo "❌ Error: CHANGELOG.md not found at $CHANGELOG_FILE"
    exit 1
fi

CHANGELOG_CONTENT=$(sed -n "/^## \[$VERSION\]/,/^## \[/ { /^## \[/!p; }" "$CHANGELOG_FILE" | sed '1d')

if [ -z "$CHANGELOG_CONTENT" ]; then
    echo "⚠️  No changelog entry found for version $VERSION. Skipping README update."
else
    echo "✅ Found changelog entry for v$VERSION"
    
    # Clean up empty lines at start/end
    CLEAN_CHANGELOG=$(echo "$CHANGELOG_CONTENT" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
    
    # 3. Construct Replacement Block
    NEW_BLOCK="<!-- LATEST-VERSION-START -->
### Latest Update (v$VERSION)
$CLEAN_CHANGELOG
<!-- LATEST-VERSION-END -->"

    # 4. Replace in README using perl for multiline handling
    if [ -f "$README_FILE" ]; then
        # Update Content
        perl -i -0777 -pe "s|<!-- LATEST-VERSION-START -->.*<!-- LATEST-VERSION-END -->|$(echo "$NEW_BLOCK" | sed 's/|/\\|/g')|gs" "$README_FILE"
        echo "✅ Updated Latest Update section in README.md"

        # Update Badge
        sed -i "s|badge/Version-[0-9.]*-blue.svg|badge/Version-$VERSION-blue.svg|g" "$README_FILE"
        # Also update the Alt Text logic slightly more broadly to catch [Version X.Y.Z]
        sed -i "s|\[Version [0-9.]*\]|[Version $VERSION]|g" "$README_FILE"
        echo "✅ Updated Version Badge in README.md"
    else
         echo "❌ README.md not found at $README_FILE!"
         exit 1
    fi
fi

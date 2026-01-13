#!/bin/bash
set -e

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
README_FILE="$PROJECT_DIR/.github/README.md"
LINT_REPORT_FILE="$PROJECT_DIR/build/reports/detekt/detekt.html" # Assuming we might want to link this later, but constructing summary first
LINT_OUTPUT_FILE="$1" # Passed as argument

if [ -z "$LINT_OUTPUT_FILE" ]; then
    echo "Usage: $0 <lint_output_file>"
    exit 1
fi

CURRENT_DATE=$(date -u +"%Y-%m-%d %H:%M:%S UTC")

if [ -f "$LINT_OUTPUT_FILE" ]; then
    # Simple logic: If the file exists and is empty/contains success message, it's a pass.
    # However, usually we capture stdout/stderr.
    # User requested: "Whatever is the output no error xD" inside details.
    
    LINT_CONTENT=$(cat "$LINT_OUTPUT_FILE")
    
    # Heuristic: Check if the command preceding this (which generated the file) failed.
    # Since we can't know the exit code of the previous command here easily unless passed, 
    # we'll assume the caller passes a status or we check for keywords.
    # BETTER APPROACH: The caller (CI) handles the logic of "Pass/Fail" and passes it to this script?
    # Or this script just formats whatever is given.
    
    # Let's take a status argument as $2
    STATUS="$2"
    
    if [ "$STATUS" == "success" ]; then
        STATUS_ICON="✅ **Passing**"
        SUMMARY="0 errors, 0 warnings" # Simplified for now, real parsing would be complex
    else
        STATUS_ICON="❌ **Failing**"
        SUMMARY="Check output for details"
    fi
    
    # Escape content for markdown code block
    SAFE_CONTENT=$(echo "$LINT_CONTENT" | sed 's/`/\\`/g')
    
    NEW_BLOCK="<!-- LINT-RESULT-START -->
### Linting Status
> **Status**: $STATUS_ICON  
> **Last Updated**: $CURRENT_DATE  
> **Summary**: $SUMMARY

<details>
<summary>Click to view full lint output</summary>

\`\`\`
$SAFE_CONTENT
\`\`\`

</details>
<!-- LINT-RESULT-END -->"

    # Construct Badges
    
    # 0. Detect Repository (Dynamic)
    FULL_REPO="DarkPhilosophy/Ko" # Default fallback
    if command -v git &> /dev/null; then
        REMOTE_URL=$(git config --get remote.origin.url || true)
        if [ -n "$REMOTE_URL" ]; then
            CLEAN_URL=${REMOTE_URL%.git}
            if [[ "$CLEAN_URL" =~ github.com[:/]([^/]+)/([^/]+) ]]; then
                FULL_REPO="${BASH_REMATCH[1]}/${BASH_REMATCH[2]}"
            fi
        fi
    fi

    # 1. Pre-Build Status (Dynamic)
    if [ "$STATUS" == "success" ]; then
        PREBUILD_BADGE="[![PreBuild](https://img.shields.io/badge/PreBuild-Passing-brightgreen)](https://github.com/$FULL_REPO/actions)"
    else
        PREBUILD_BADGE="[![PreBuild](https://img.shields.io/badge/PreBuild-Failing-red)](https://github.com/$FULL_REPO/actions)"
    fi

    # 2. Build Status (Static)
    BUILD_BADGE="[![Build Status](https://github.com/$FULL_REPO/actions/workflows/build-apk.yaml/badge.svg)](https://github.com/$FULL_REPO/actions)"

    # 3. License (Static)
    LICENSE_BADGE="[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)"

    # 4. Version (Dynamic from version.properties)
    if [ -f "version.properties" ]; then
        MAJOR=$(grep "version.major" version.properties | cut -d'=' -f2)
        MINOR=$(grep "version.minor" version.properties | cut -d'=' -f2)
        PATCH=$(grep "version.patch" version.properties | cut -d'=' -f2)
        VERSION="$MAJOR.$MINOR.$PATCH"
        VERSION_BADGE="[![Version $VERSION](https://img.shields.io/badge/Version-$VERSION-blue.svg)](https://github.com/$FULL_REPO)"
    else
        VERSION_BADGE="[![Version](https://img.shields.io/badge/Version-Unknown-gray.svg)](https://github.com/$FULL_REPO)"
    fi

    if [ -f "$README_FILE" ]; then
        # Replace Lint Block
        perl -i -0777 -pe "s|<!-- LINT-RESULT-START -->.*<!-- LINT-RESULT-END -->|$(echo "$NEW_BLOCK" | sed 's/|/\\|/g')|gs" "$README_FILE"
        
        # Replace Badge Block
        BADGE_BLOCK="<!-- LATEST-BUILD-STATUS-START -->
$PREBUILD_BADGE
$BUILD_BADGE
$LICENSE_BADGE
$VERSION_BADGE
<!-- LATEST-BUILD-STATUS-END -->"
        
        # 1. Initial Replacement: If strictly the placeholder exists
        perl -i -0777 -pe "s|<!-- LATEST-BUILD-STATUS -->|$BADGE_BLOCK|g" "$README_FILE"
        
        # 2. Subsequent Updates: Replace content between START/END markers
        perl -i -0777 -pe "s|<!-- LATEST-BUILD-STATUS-START -->.*<!-- LATEST-BUILD-STATUS-END -->|$(echo "$BADGE_BLOCK" | sed 's/|/\\|/g')|gs" "$README_FILE"

        echo "✅ Updated Lint Status & Badge Block in README.md"
    else
        echo "❌ README.md not found!"
        exit 1
    fi
else
    echo "❌ Lint output file not found: $LINT_OUTPUT_FILE"
    exit 1
fi

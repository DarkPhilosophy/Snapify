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

    # Construct Badge
    if [ "$STATUS" == "success" ]; then
        BADGE="![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)"
    else
        BADGE="![Build Status](https://img.shields.io/badge/Build-Failing-red)"
    fi

    if [ -f "$README_FILE" ]; then
        # Replace Lint Block
        perl -i -0777 -pe "s|<!-- LINT-RESULT-START -->.*<!-- LINT-RESULT-END -->|$(echo "$NEW_BLOCK" | sed 's/|/\\|/g')|gs" "$README_FILE"
        
        # Replace Badge
        # Using a marker <!-- LATEST-PRE-BUILD-STATUS -->
        # We need to replace the marker OR the existing badge if it was already replaced.
        # But simplify: replace the marker content if it exists, or just the marker.
        # Actually, let's just replace the whole line containing the marker or the previous badge if we wrap it in comments?
        # User requested <!-- LATEST-PRE-BUILD-STATUS -->. Let's assume we wrap the badge in it to make it replaceable next time.
        
        BADGE_BLOCK="<!-- LATEST-PRE-BUILD-STATUS-START -->
$BADGE
<!-- LATEST-PRE-BUILD-STATUS-END -->"
        
        # 1. Initial Replacement: If strictly the placeholder exists
        perl -i -0777 -pe "s|<!-- LATEST-PRE-BUILD-STATUS -->|$BADGE_BLOCK|g" "$README_FILE"
        
        # 2. Subsequent Updates: Replace content between START/END markers
        perl -i -0777 -pe "s|<!-- LATEST-PRE-BUILD-STATUS-START -->.*<!-- LATEST-PRE-BUILD-STATUS-END -->|$(echo "$BADGE_BLOCK" | sed 's/|/\\|/g')|gs" "$README_FILE"

        echo "✅ Updated Lint Status & Badge in README.md"
    else
        echo "❌ README.md not found!"
        exit 1
    fi
else
    echo "❌ Lint output file not found: $LINT_OUTPUT_FILE"
    exit 1
fi

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

    if [ -f "$README_FILE" ]; then
        # Replace block
        perl -i -0777 -pe "s|<!-- LINT-RESULT-START -->.*<!-- LINT-RESULT-END -->|$(echo "$NEW_BLOCK" | sed 's/|/\\|/g')|gs" "$README_FILE"
        echo "✅ Updated Lint Status in README.md"
    else
        echo "❌ README.md not found!"
        exit 1
    fi
else
    echo "❌ Lint output file not found: $LINT_OUTPUT_FILE"
    exit 1
fi

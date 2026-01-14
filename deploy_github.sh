#!/bin/bash
set -e

# deploy_github.sh - Automates Build, Secrets, and Deployment

# 0. Ensure Secrets are Generated & Synced
if [ -f "./generate_secrets.sh" ]; then
    echo "🔐 Syncing Secrets..."
    ./generate_secrets.sh
else
    echo "⚠️  generate_secrets.sh not found. Skipping secrets sync."
fi

# 1. Argument Parsing
IS_FIRST_RELEASE=false
COMMIT_MSG=""
ARGS_FLAGS=""

if [ "$1" == "--first" ]; then
    IS_FIRST_RELEASE=true
    COMMIT_MSG="$2"
    shift 2
    ARGS_FLAGS="$@" # Capture remaining flags if any
else
    COMMIT_MSG="$1"
    shift 1
    ARGS_FLAGS="$@"
fi

if [ -z "$COMMIT_MSG" ]; then
    echo "❌ Error: Commit message is required!"
    echo "Usage: ./deploy_github.sh \"your commit message\" [flags]"
    echo "       ./deploy_github.sh --first \"v1.0.0 Initial Release\""
    exit 1
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)

echo "========================================"
echo "🚀 Starting Deployment for $BRANCH"
echo "========================================"

# 2. "First Release" Reset Logic
if [ "$IS_FIRST_RELEASE" = true ]; then
    echo "✨ FIRST RELEASE DETECTED! Resetting project details..."
    
    # Reset Version to 1.0.0
    if [ -f "version.properties" ]; then
        sed -i 's/^version.major=.*/version.major=1/' version.properties
        sed -i 's/^version.minor=.*/version.minor=0/' version.properties
        sed -i 's/^version.patch=.*/version.patch=0/' version.properties
        sed -i 's/^version.code=.*/version.code=1/' version.properties
        echo "✅ Version reset to 1.0.0 (Code: 1)"
    fi
    
    # Reset/Create Changelog
    echo "# Changelog" > CHANGELOG.md
    echo "" >> CHANGELOG.md
    echo "## v1.0.0 - $(date +%Y-%m-%d)" >> CHANGELOG.md
    echo "- $COMMIT_MSG" >> CHANGELOG.md
    echo "✅ CHANGELOG.md initialized."
fi

# 3. Build
echo "🛠️  Running Build..."
./gradlew assembleDebug

# 4. Git Operations
if [[ -n $(git status --porcelain) ]] || [[ "$ARGS_FLAGS" == *"--amend"* ]] || [ "$IS_FIRST_RELEASE" = true ]; then
    echo "📸 Committing changes..."
    git add .
    
    if [[ "$ARGS_FLAGS" == *"--amend"* ]]; then
        echo "⚠️  Amending previous commit..."
        git commit --amend -m "$COMMIT_MSG"
        echo "⬆️  Force Pushing to origin/$BRANCH..."
        git push origin "$BRANCH" --force
    else
        git commit -m "$COMMIT_MSG"
        echo "⬆️  Pushing to origin/$BRANCH..."
        git push origin "$BRANCH"
    fi
    
    echo "✅ Deployed successfully!"
else
    echo "✨ No changes to commit."
fi

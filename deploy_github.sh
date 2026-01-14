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
    ARGS_FLAGS="$@" # Capture remaining flags
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

# 2. Build Check
echo "🛠️  Running Build..."
./gradlew assembleDebug

# 3. Deployment Logic
if [ "$IS_FIRST_RELEASE" = true ]; then
    echo "☢️  FIRST RELEASE DETECTED: Nuking Git History..."
    
    # 3a. Reset Changelog to single entry
    echo "# Changelog" > CHANGELOG.md
    echo "" >> CHANGELOG.md
    echo "## $(date +%Y-%m-%d) - Initial Release" >> CHANGELOG.md
    echo "- $COMMIT_MSG" >> CHANGELOG.md
    echo "✅ CHANGELOG.md reset."

    echo "📸 Creating Fresh Root Commit..."
    
    # 3b. Orphan Branch Strategy to Nuke History
    # Create new orphan branch (clean history)
    git checkout --orphan temp_root_branch
    
    # Add all files (respecting .gitignore)
    git add -A
    
    # Commit
    git commit -m "$COMMIT_MSG"
    
    # Delete old main
    git branch -D main
    
    # Rename current to main
    git branch -m main
    
    echo "⬆️  Force Pushing Clean History to origin/main..."
    git push -f origin main
    
    echo "✅ HISTORY ERASED. Project successfully reset to single commit."
    exit 0

else
    # 4. Standard Deployment (Append History)
    if [[ -n $(git status --porcelain) ]] || [[ "$ARGS_FLAGS" == *"--amend"* ]]; then
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
fi

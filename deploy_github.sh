#!/bin/bash
set -e

# deploy_github.sh - Automates Build, README sync, and Deployment

if [ -z "$1" ]; then
    echo "❌ Error: Commit message is required!"
    echo "Usage: ./deploy_github.sh \"your commit message\" [flags]"
    exit 1
fi

COMMIT_MSG="$1"
BRANCH=$(git rev-parse --abbrev-ref HEAD)

echo "========================================"
echo "🚀 Starting Deployment for $BRANCH"
echo "========================================"

# 1. Sync & Build
echo "📄 CI will handle README updates."
echo "🛠️  Running Build..."
./gradlew assembleDebug

# 3. Git Operations
# 3. Git Operations
if [[ -n $(git status --porcelain) ]] || [[ "$2" == "--amend" ]]; then
    echo "📸 Committing changes..."
    git add .
    
    if [[ "$2" == "--amend" ]]; then
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

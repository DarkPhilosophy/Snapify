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
if [[ -n $(git status --porcelain) ]]; then
    echo "📸 Committing changes..."
    git add .
    git commit -m "$COMMIT_MSG"
    
    echo "⬆️  Pushing to origin/$BRANCH..."
    git push origin "$BRANCH"
    
    echo "✅ Deployed successfully!"
else
    echo "✨ No changes to commit."
fi

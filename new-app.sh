#!/usr/bin/env bash
# Create a brand-new app repo from your template in one command.
#
#   ./new-app.sh quiz-cash com.app.vv.quizcash "Quiz Cash"
#
# Requires: GitHub CLI (gh) logged in (`gh auth login`) and git.
# Optional: TEMPLATE_REPO=youruser/android-template (defaults to <your gh user>/android-template)
#           PARENT_DIR=~/Projects (where the new project folder is created; default: current dir)
set -euo pipefail

REPO="${1:-}"
PKG="${2:-}"
NAME="${3:-}"
VISIBILITY="--private"
[[ "${4:-}" == "--public" ]] && VISIBILITY="--public"

if [[ -z "$REPO" || -z "$PKG" || -z "$NAME" ]]; then
  echo "Usage: $0 <repo-name> <package.id> \"App Name\" [--public]" >&2
  exit 1
fi

command -v gh >/dev/null || { echo "Install GitHub CLI first: https://cli.github.com" >&2; exit 1; }

TEMPLATE_REPO="${TEMPLATE_REPO:-$(gh api user -q .login)/android-template}"
PARENT_DIR="${PARENT_DIR:-$PWD}"

cd "$PARENT_DIR"
echo "Creating $REPO from $TEMPLATE_REPO ..."
gh repo create "$REPO" --template "$TEMPLATE_REPO" "$VISIBILITY" --clone

cd "$REPO"
chmod +x scripts/rename.sh
./scripts/rename.sh "$PKG" "$NAME"

git add -A
git commit -m "Initial setup: $NAME ($PKG)"
git push -u origin HEAD

echo
echo "Done. Open this folder in Android Studio: $PARENT_DIR/$REPO"

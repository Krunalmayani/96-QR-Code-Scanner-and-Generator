#!/usr/bin/env bash
# Rename the template to a real app (package id + app name). Run from the repo root.
#
#   ./scripts/rename.sh com.app.vv.quizcash "Quiz Cash"
#
# Works on macOS, Linux and Git Bash on Windows.
set -euo pipefail

NEW_PKG="${1:-}"
APP_NAME="${2:-}"

if [[ -z "$NEW_PKG" || -z "$APP_NAME" ]]; then
  echo "Usage: $0 <new.package.id> \"App Name\"" >&2
  exit 1
fi

if ! [[ "$NEW_PKG" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
  echo "Invalid package id: $NEW_PKG (use lowercase letters, digits, underscores, dots)" >&2
  exit 1
fi

cd "$(dirname "$0")/.."

OLD_PKG="$(sed -n 's/.*namespace = "\([^"]*\)".*/\1/p' app/build.gradle.kts | head -n1)"
if [[ -z "$OLD_PKG" ]]; then
  echo "Could not detect current package from app/build.gradle.kts" >&2
  exit 1
fi
if [[ "$OLD_PKG" == "$NEW_PKG" ]]; then
  echo "Package is already $NEW_PKG, nothing to do."
  exit 0
fi

OLD_PATH="${OLD_PKG//.//}"
NEW_PATH="${NEW_PKG//.//}"
JAVA_ROOT="app/src/main/java"

# Escape a string for use on the right-hand side of sed s|..|..|
esc() { printf '%s' "$1" | sed -e 's/[&|\\]/\\&/g'; }

# 1) Replace package id inside text files (dots must be escaped on the left side)
OLD_RE="${OLD_PKG//./\\.}"
NEW_ESC="$(esc "$NEW_PKG")"
while IFS= read -r -d '' f; do
  sed -i.bak "s|$OLD_RE|$NEW_ESC|g" "$f" && rm -f "$f.bak"
done < <(find app -type f \( -name '*.kt' -o -name '*.java' -o -name '*.kts' -o -name '*.xml' -o -name '*.json' -o -name '*.pro' \) -print0)

# 2) Move source directories
if [[ -d "$JAVA_ROOT/$OLD_PATH" ]]; then
  mkdir -p "$JAVA_ROOT/$NEW_PATH"
  cp -R "$JAVA_ROOT/$OLD_PATH/." "$JAVA_ROOT/$NEW_PATH/"
  rm -rf "$JAVA_ROOT/$OLD_PATH"
  # prune now-empty parent dirs (e.g. com/app/vv)
  find "$JAVA_ROOT" -type d -empty -delete 2>/dev/null || true
fi

# 3) App name (XML-escape & and ', then sed-escape)
XML_NAME="${APP_NAME//&/&amp;}"
XML_NAME="${XML_NAME//\'/\\\'}"
NAME_ESC="$(esc "$XML_NAME")"
sed -i.bak "s|<string name=\"app_name\">[^<]*</string>|<string name=\"app_name\">$NAME_ESC</string>|" \
  app/src/main/res/values/strings.xml && rm -f app/src/main/res/values/strings.xml.bak

# 4) Gradle root project name
ROOT_NAME="$(printf '%s' "$APP_NAME" | tr -cd '[:alnum:] _-' | tr -s ' ')"
sed -i.bak "s|rootProject.name = \".*\"|rootProject.name = \"$(esc "$ROOT_NAME")\"|" settings.gradle.kts && rm -f settings.gradle.kts.bak

echo "Renamed:"
echo "  package  $OLD_PKG -> $NEW_PKG"
echo "  app name -> $APP_NAME"
echo
echo "Next: replace app/google-services.json with the real one from Firebase,"
echo "      and set your real AdMob ids in app/build.gradle.kts (see README)."

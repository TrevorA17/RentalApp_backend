#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 1 ] || [ -z "${1:-}" ]; then
    echo "Usage: $0 \"migration description\"" >&2
    echo "Example: $0 \"add listing favourites column\"" >&2
    exit 1
fi

DESCRIPTION="$1"
TIMESTAMP=$(date -u +%Y%m%d%H%M%S)
SNAKE_DESC=$(printf '%s' "$DESCRIPTION" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '_' | sed 's/^_//;s/_$//')

if [ -z "$SNAKE_DESC" ]; then
    echo "Description produced an empty filename slug." >&2
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MIGRATION_DIR="$(cd "$SCRIPT_DIR/.." && pwd)/src/main/resources/db/migration"
FILENAME="V${TIMESTAMP}__${SNAKE_DESC}.sql"
TARGET="$MIGRATION_DIR/$FILENAME"

mkdir -p "$MIGRATION_DIR"
if [ -e "$TARGET" ]; then
    echo "Refusing to overwrite existing migration: $TARGET" >&2
    exit 1
fi

cat >"$TARGET" <<EOF
-- Migration: $DESCRIPTION
-- Created:   $TIMESTAMP (UTC)

EOF

echo "Created: $TARGET"

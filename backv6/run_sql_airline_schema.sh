#!/bin/bash
set -euo pipefail

SQLFILE="$(cd "$(dirname "$0")" && pwd)/create_airline_schema.sql"
CONNECT_STR="AEROLINEA/123@64.225.58.196:1521/XEPDB1"

if ! command -v sqlplus >/dev/null 2>&1; then
  echo "Error: sqlplus no está instalado. Instala Instant Client de Oracle." >&2
  exit 1
fi

echo "Ejecutando script: $SQLFILE"
sqlplus -s "$CONNECT_STR" @"$SQLFILE"
echo "Listo."



#!/bin/bash
set -euo pipefail

SQLFILE="$(cd "$(dirname "$0")" && pwd)/oracle_schema.sql"
CONNECT_STR="AEROLINEA/123@64.225.58.196:1521/XEPDB1"

if ! command -v sqlplus >/dev/null 2>&1; then
  echo "Error: sqlplus no está instalado. Instala Instant Client de Oracle." >&2
  exit 1
fi

echo "Ejecutando script de esquema de vuelos: $SQLFILE"
echo "Conectando a: $CONNECT_STR"
sqlplus -s "$CONNECT_STR" @"$SQLFILE"
echo "Esquema de vuelos ejecutado exitosamente."

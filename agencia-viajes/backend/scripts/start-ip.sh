#!/bin/zsh

# Detectar IP LAN
IP=$(python3 - <<'PY'
import socket
s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
try:
    s.connect(("8.8.8.8",80))
    print(s.getsockname()[0])
finally:
    s.close()
PY
)

# Exponer variables para CORS y logs
export FRONTEND_HOST="$IP"
export FRONTEND_PORT="${FRONTEND_PORT:-3000}"
export PORT="${PORT:-5001}"

echo "🚀 Backend Node en http://0.0.0.0:${PORT} (CORS permite http://${FRONTEND_HOST}:${FRONTEND_PORT})"

exec npm run dev



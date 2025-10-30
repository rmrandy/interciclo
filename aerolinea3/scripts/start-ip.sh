#!/bin/zsh

# Detectar IP y exportarla para Vite
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

export VITE_IP="$IP"
export VITE_HOST="$IP"

echo "🌐 Frontend Vue servirá en http://${VITE_HOST}:5173 (o el puerto configurado)"

exec npm run dev



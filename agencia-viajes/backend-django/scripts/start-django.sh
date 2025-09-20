#!/bin/zsh

# Activar venv si existe
if [ -d ".venv" ]; then
  source .venv/bin/activate 2>/dev/null || true
fi

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

export FRONTEND_PORT="${FRONTEND_PORT:-5173}"
export DJANGO_DEBUG=1
export DJANGO_SECRET_KEY="dev-secret-key"

echo "🌐 Django servirá API en http://${IP}:5001"

# Verificar que Django esté instalado
python3 - <<'PY'
try:
    import django  # noqa: F401
    print('[OK] Django detectado')
except Exception:
    print('[ERROR] Django no está instalado en este entorno. Ejecuta:')
    print('       source .venv/bin/activate && pip install -r requirements.txt')
    raise SystemExit(1)
PY

exec python3 manage.py runserver 0.0.0.0:5001



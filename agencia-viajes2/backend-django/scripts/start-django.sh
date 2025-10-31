#!/bin/zsh

# Uso: ./scripts/start-django.sh [PUERTO]
# Ejemplo: ./scripts/start-django.sh 5001

# Puerto por defecto o del primer argumento
DJANGO_PORT="${1:-5001}"

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
export DJANGO_PORT="${DJANGO_PORT}"

# Forzar destino del backend de Aerolínea (proxy) - SOLO COMO FALLBACK
# NOTA: Si hay aerolíneas configuradas en la BD (colección airlines con enabled=True),
# esas configuraciones tienen prioridad sobre estas variables de entorno.
# Estos valores solo se usan si NO hay aerolíneas activas en la BD.
export AIRLINE_PROTOCOL="http"
export AIRLINE_HOST="${IP}"  # Usa la IP detectada automáticamente
export AIRLINE_PORT="8080"
export AIRLINE_BASE_PATH="/api"
export AIRLINE_TIMEOUT_MS="40000"  # 40 segundos de timeout

echo "🌐 Django servirá API en http://${IP}:${DJANGO_PORT}"

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

exec python3 manage.py runserver 0.0.0.0:${DJANGO_PORT}



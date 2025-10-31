Backend Django (API Agencia de Viajes)

Comandos rápidos (macOS):

1) Crear venv e instalar dependencias
```
cd agencia-viajes/backend-django
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

2) Configurar conexión Mongo (archivo .env)
```
echo "MONGODB_URI=mongodb+srv://USUARIO:PASS@CLUSTER/hotel?retryWrites=true&w=majority" > .env
```

3) Ejecutar en 0.0.0.0:5001
```
chmod +x scripts/start-django.sh
./scripts/start-django.sh
```

Endpoints:
- GET /health
- POST /api/auth/register
- POST /api/auth/login
- GET /api/auth/profile (Authorization: Bearer <token>)



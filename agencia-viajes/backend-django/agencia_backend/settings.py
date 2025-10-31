import os
from pathlib import Path
from dotenv import load_dotenv

load_dotenv()

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = os.environ.get('DJANGO_SECRET_KEY', 'dev-secret-key')
DEBUG = os.environ.get('DJANGO_DEBUG', '1') == '1'

ALLOWED_HOSTS = ['*']

INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'rest_framework',
    'corsheaders',
    'api',
]

MIDDLEWARE = [
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'agencia_backend.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'agencia_backend.wsgi.application'

# MongoDB via pymongo (usamos una capa simple en la app)
# Default: MongoDB Atlas (nube)
MONGODB_URI = os.environ.get('MONGODB_URI', 'mongodb+srv://admin:123@cluster0.svqq8ek.mongodb.net/agencia-viajes?retryWrites=true&w=majority&appName=Cluster0')

AUTH_PASSWORD_VALIDATORS = []

LANGUAGE_CODE = 'es'
TIME_ZONE = 'UTC'
USE_I18N = True
USE_TZ = True

STATIC_URL = 'static/'

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

# CORS: permitir dinámicamente en desarrollo
FRONTEND_PORT = os.environ.get('FRONTEND_PORT', '5173')
CORS_ALLOW_CREDENTIALS = True

if DEBUG:
    # En desarrollo, permitir cualquier origen para evitar bloqueos CORS
    CORS_ALLOW_ALL_ORIGINS = True
else:
    import re
    CORS_ALLOWED_ORIGIN_REGEXES = [
        re.compile(rf"^https?://[^/]+:{FRONTEND_PORT}$")
    ]

REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': [],
}



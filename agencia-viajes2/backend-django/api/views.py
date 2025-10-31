import os
import bcrypt
import jwt
from datetime import datetime, timedelta
from decimal import Decimal
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from django.views import View
from pymongo import MongoClient
from django.conf import settings
import json
import re
import requests
from bson import ObjectId
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Optional, Union, List, Dict, Any


def get_db():
    client = MongoClient(settings.MONGODB_URI)
    try:
        default_db = client.get_default_database()
    except Exception:
        default_db = None
    if default_db is not None:
        return default_db
    return client.get_database('agencia-viajes')


def parse_request_data(request):
    try:
        raw = request.body.decode('utf-8') if request.body else ''
        if raw:
            return json.loads(raw)
    except Exception:
        pass
    try:
        return request.POST.dict()
    except Exception:
        return {}


def health_view(request):
    return JsonResponse({
        'success': True,
        'message': 'Servidor Django OK',
        'timestamp': datetime.utcnow().isoformat() + 'Z'
    })



def generate_token(user_id: str) -> str:
    payload = {
        'sub': user_id,
        'exp': datetime.utcnow() + timedelta(days=7),
        'iat': datetime.utcnow()
    }
    return jwt.encode(payload, settings.SECRET_KEY, algorithm='HS256')


email_pattern = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


@csrf_exempt
def register_view(request):
    if request.method != 'POST':
        return JsonResponse({'success': False, 'message': 'Método no permitido'}, status=405)
    try:
        data = parse_request_data(request)
        firstName = (data.get('firstName') or '').strip()
        lastName = (data.get('lastName') or '').strip()
        email = (data.get('email') or '').strip().lower()
        password = data.get('password') or ''
        age_raw = data.get('age')
        country = (data.get('country') or '').strip()
        passportNumber = (data.get('passportNumber') or '').strip()
        phone = (data.get('phone') or '').strip()
        address = (data.get('address') or '').strip()

        # Validaciones básicas
        if not firstName or not lastName:
            return JsonResponse({'success': False, 'message': 'Nombre y apellido son obligatorios'}, status=400)
        if not email or not email_pattern.match(email):
            return JsonResponse({'success': False, 'message': 'Email inválido'}, status=400)
        if not password or len(password) < 8:
            return JsonResponse({'success': False, 'message': 'La contraseña debe tener al menos 8 caracteres'}, status=400)
        try:
            age = int(age_raw)
        except Exception:
            return JsonResponse({'success': False, 'message': 'Edad inválida'}, status=400)
        if age < 18:
            return JsonResponse({'success': False, 'message': 'Debes ser mayor de 18 años'}, status=400)
        if not country:
            return JsonResponse({'success': False, 'message': 'País es obligatorio'}, status=400)
        if not passportNumber or len(passportNumber) < 5:
            return JsonResponse({'success': False, 'message': 'Pasaporte inválido (mínimo 5 caracteres)'}, status=400)

        db = get_db()
        if db.users.find_one({'email': email}):
            return JsonResponse({'success': False, 'message': 'El usuario ya existe con este email'}, status=400)

        salt = bcrypt.gensalt(rounds=12)
        password_hash = bcrypt.hashpw(password.encode('utf-8'), salt).decode('utf-8')

        user_doc = {
            'firstName': firstName,
            'lastName': lastName,
            'email': email,
            'password': password_hash,
            'age': age,
            'country': country,
            'passportNumber': passportNumber,
            'phone': phone or None,
            'address': address or None,
            'role': 'user',
            'isActive': True,
            'createdAt': datetime.utcnow(),
            'updatedAt': datetime.utcnow(),
        }
        result = db.users.insert_one(user_doc)

        token = generate_token(str(result.inserted_id))
        return JsonResponse({
            'success': True,
            'message': 'Usuario registrado exitosamente',
            'data': {
                'user': {
                    'id': str(result.inserted_id),
                    'firstName': firstName,
                    'lastName': lastName,
                    'email': email,
                    'age': age,
                    'country': country,
                    'passportNumber': passportNumber,
                    'phone': phone or None,
                    'address': address or None,
                    'role': 'user',
                    'isActive': True,
                },
                'token': token,
            }
        }, status=201)
    except Exception as e:
        return JsonResponse({'success': False, 'message': 'Error interno del servidor'}, status=500)


@csrf_exempt
def login_view(request):
    if request.method != 'POST':
        return JsonResponse({'success': False, 'message': 'Método no permitido'}, status=405)
    try:
        data = parse_request_data(request)
        email = (data.get('email') or '').strip().lower()
        password = data.get('password') or ''

        db = get_db()
        user = db.users.find_one({'email': email})
        if not user or not user.get('password'):
            return JsonResponse({'success': False, 'message': 'Credenciales inválidas'}, status=401)

        if not user.get('isActive', True):
            return JsonResponse({'success': False, 'message': 'Cuenta desactivada'}, status=401)

        if not bcrypt.checkpw(password.encode('utf-8'), user['password'].encode('utf-8')):
            return JsonResponse({'success': False, 'message': 'Credenciales inválidas'}, status=401)

        token = generate_token(str(user['_id']))
        return JsonResponse({
            'success': True,
            'message': 'Inicio de sesión exitoso',
            'data': {
                'user': {
                    'id': str(user['_id']),
                    'firstName': user.get('firstName',''),
                    'lastName': user.get('lastName',''),
                    'email': user.get('email',''),
                    'role': user.get('role','user'),
                    'isActive': user.get('isActive', True),
                },
                'token': token,
            }
        })
    except Exception as e:
        print(f"❌ Error en login_view: {type(e).__name__}: {str(e)}")
        import traceback
        traceback.print_exc()
        return JsonResponse({'success': False, 'message': f'Error interno del servidor: {type(e).__name__}'}, status=500)


@csrf_exempt
def profile_view(request):
    # Para simplificar, solo valida token en Authorization: Bearer <token>
    auth = request.headers.get('Authorization', '')
    if not auth.startswith('Bearer '):
        return JsonResponse({'success': False, 'message': 'No autorizado'}, status=401)
    token = auth.split(' ',1)[1]
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=['HS256'])
        user_id = payload.get('sub')
        db = get_db()
        user = db.users.find_one({'_id': __import__('bson').ObjectId(user_id)})
        if not user:
            return JsonResponse({'success': False, 'message': 'No autorizado'}, status=401)
        return JsonResponse({
            'success': True,
            'data': { 'user': {
                'id': str(user['_id']),
                'firstName': user.get('firstName',''),
                'lastName': user.get('lastName',''),
                'email': user.get('email',''),
                'role': user.get('role','user'),
                'isActive': user.get('isActive', True),
            }}
        })
    except Exception:
        return JsonResponse({'success': False, 'message': 'No autorizado'}, status=401)


# --------- Airlines CRUD (sin auth por simplicidad) ---------

airline_required_fields = ['name', 'code', 'protocol', 'host', 'port', 'basePath']


def normalize_airline_payload(data):
    return {
        'name': (data.get('name') or '').strip(),
        'code': (data.get('code') or '').strip().upper(),
        'protocol': (data.get('protocol') or 'http').strip(),
        'host': (data.get('host') or '').strip(),
        'port': int(data.get('port') or 80),
        'basePath': (data.get('basePath') or '/').strip(),
        'apiKey': (data.get('apiKey') or '').strip(),  # API Key para autenticación
        'endpoints': data.get('endpoints') or {
            'search': '/search',
            'book': '/book',
            'cancel': '/cancel',
            'health': '/health'
        },
        'enabled': bool(data.get('enabled', True)),
        'timeoutMs': int(data.get('timeoutMs') or 2000),
        'notes': (data.get('notes') or '').strip(),
        'createdAt': datetime.utcnow(),
        'updatedAt': datetime.utcnow(),
    }


@csrf_exempt
def airlines_list_view(request):
    db = get_db()
    if request.method == 'GET':
        items = list(db.airlines.find().sort('name', 1))
        for it in items:
            it['_id'] = str(it['_id'])
        return JsonResponse({'success': True, 'data': items})
    if request.method == 'POST':
        try:
            data = parse_request_data(request)
            payload = normalize_airline_payload(data)
            for f in airline_required_fields:
                if not payload.get(f):
                    return JsonResponse({'success': False, 'message': f'Campo requerido faltante: {f}'}, status=400)
            res = db.airlines.insert_one(payload)
            payload['_id'] = str(res.inserted_id)
            return JsonResponse({'success': True, 'data': payload}, status=201)
        except Exception:
            return JsonResponse({'success': False, 'message': 'Error al crear aerolínea'}, status=500)
    return JsonResponse({'success': False, 'message': 'Método no permitido'}, status=405)


@csrf_exempt
def airlines_detail_view(request, airline_id: str):
    from bson import ObjectId
    db = get_db()
    try:
        oid = ObjectId(airline_id)
    except Exception:
        return JsonResponse({'success': False, 'message': 'ID inválido'}, status=400)

    if request.method == 'GET':
        doc = db.airlines.find_one({'_id': oid})
        if not doc:
            return JsonResponse({'success': False, 'message': 'No encontrado'}, status=404)
        doc['_id'] = str(doc['_id'])
        return JsonResponse({'success': True, 'data': doc})
    if request.method in ['PATCH', 'PUT']:
        try:
            data = parse_request_data(request)
            updates = normalize_airline_payload(data)
            updates.pop('createdAt', None)
            updates['updatedAt'] = datetime.utcnow()
            db.airlines.update_one({'_id': oid}, {'$set': updates})
            doc = db.airlines.find_one({'_id': oid})
            doc['_id'] = str(doc['_id'])
            print(f"✅ Aerolínea actualizada en BD: {doc.get('name')} - {doc.get('host')}:{doc.get('port')}")
            return JsonResponse({'success': True, 'data': doc})
        except Exception:
            return JsonResponse({'success': False, 'message': 'Error al actualizar'}, status=500)
    if request.method == 'DELETE':
        db.airlines.delete_one({'_id': oid})
        return JsonResponse({'success': True})
    return JsonResponse({'success': False, 'message': 'Método no permitido'}, status=405)


@csrf_exempt
def airlines_active_view(request):
    """
    Obtiene la configuración de la aerolínea activa (enabled=True).
    Útil para que el frontend sepa a qué servidor se está conectando.
    """
    if request.method != 'GET':
        return JsonResponse({'success': False, 'message': 'Método no permitido'}, status=405)
    
    try:
        db = get_db()
        airline = db.airlines.find_one({'enabled': True})
        
        if not airline:
            return JsonResponse({
                'success': False, 
                'message': 'No hay aerolínea activa configurada'
            }, status=404)
        
        airline['_id'] = str(airline['_id'])
        
        # También retornar la URL completa construida
        protocol = airline.get('protocol', 'http')
        host = airline.get('host', 'localhost')
        port = airline.get('port', 8080)
        base = airline.get('basePath', '/api')
        full_url = f"{protocol}://{host}:{port}{base}"
        
        return JsonResponse({
            'success': True, 
            'data': airline,
            'fullUrl': full_url,
            'message': f'Usando: {airline.get("name")}'
        })
    except Exception as e:
        return JsonResponse({
            'success': False, 
            'message': f'Error obteniendo aerolínea activa: {str(e)}'
        }, status=500)


# --------- Proxy hacia backend de aerolínea ---------

def get_active_airline():
    """
    Obtiene la aerolínea activa desde la base de datos.
    Retorna el documento completo o None si no hay aerolínea activa.
    """
    try:
        db = get_db()
        airline = db.airlines.find_one({'enabled': True})
        return airline
    except Exception as e:
        print(f"❌ Error obteniendo aerolínea activa: {e}")
        return None


def airline_origin_base(request=None):
    """
    Obtiene la URL base de la aerolínea SOLO desde la base de datos.
    Ya NO usa fallback a variables de entorno.
    Retorna None si no hay aerolínea activa configurada.
    """
    airline = get_active_airline()
    
    if not airline:
        print("❌ No se encontró aerolínea activa en BD. Debes configurar una aerolínea en el panel de Admin.")
        return None
    
    # Usar configuración de la BD
    protocol = airline.get('protocol', 'http').strip()
    host = airline.get('host', 'localhost').strip()
    port = str(airline.get('port', 8080)).strip()
    base = airline.get('basePath', '/api').strip()
    
    if not base.startswith('/'):
        base = '/' + base
    
    url = f"{protocol}://{host}:{port}{base}"
    api_key_info = f" con API Key: {airline.get('apiKey', 'N/A')[:10]}..." if airline.get('apiKey') else " sin API Key"
    print(f"✅ Usando aerolínea: {airline.get('name')} ({url}){api_key_info}")
    
    return url


def airline_timeout_seconds():
    """
    Obtiene el timeout de la aerolínea SOLO desde la base de datos.
    Default: 40 segundos si no está configurado.
    """
    airline = get_active_airline()
    
    if airline and 'timeoutMs' in airline:
        ms = int(airline.get('timeoutMs', 40000))
        return max(3, ms // 1000)
    
    return 40  # 40 segundos por defecto


def get_airline_headers():
    """
    Retorna headers con el API Key de la aerolínea activa.
    SOLO usa aerolíneas configuradas en el panel, no sistema antiguo.
    """
    headers = {'Content-Type': 'application/json'}
    
    airline = get_active_airline()
    if airline and airline.get('apiKey'):
        api_key = airline.get('apiKey').strip()
        if api_key:
            headers['X-API-Key'] = api_key
            print(f"🔑 Usando API Key de aerolínea '{airline.get('name')}': {api_key[:10]}...")
    else:
        print(f"⚠️  Aerolínea sin API Key configurado")
    
    return headers


def safe_get(url, timeout=None, params=None):
    try:
        read_timeout = timeout if isinstance(timeout, (int, float)) else airline_timeout_seconds()
        headers = get_airline_headers()  # Usar API Key de la aerolínea activa
        r = requests.get(url, params=params or {}, headers=headers, timeout=(3.0, read_timeout))
        r.raise_for_status()
        ct = r.headers.get('content-type', '')
        if 'application/json' in ct:
            return r.json()
        return {'raw': r.text}
    except Exception as e:
        return {'error': str(e)}


def safe_post(url, json_body=None, timeout=None):
    try:
        read_timeout = timeout if isinstance(timeout, (int, float)) else airline_timeout_seconds()
        headers = get_airline_headers()  # Usar API Key de la aerolínea activa
        r = requests.post(url, json=json_body or {}, headers=headers, timeout=(3.0, read_timeout))
        r.raise_for_status()
        ct = r.headers.get('content-type', '')
        if 'application/json' in ct:
            return r.json()
        return {'raw': r.text}
    except Exception as e:
        return {'error': str(e)}


def stream_get(url, timeout=None):
    try:
        read_timeout = timeout if isinstance(timeout, (int, float)) else airline_timeout_seconds()
        headers = get_airline_headers()  # Usar API Key de la aerolínea activa
        r = requests.get(url, headers=headers, timeout=(3.0, read_timeout))
        r.raise_for_status()
        return r
    except Exception as e:
        return e

def normalize_flights_response(data):
    if isinstance(data, dict) and 'flights' in data:
        return data
    # Si la aerolínea devolviera un arreglo plano
    if isinstance(data, list):
        return { 'success': True, 'flights': data }
    return { 'success': False, 'flights': [] }


def build_airline_base_url_from_doc(airline: dict) -> str:
    """Construye la URL base (protocol://host:port/basePath) para una aerolínea concreta."""
    protocol = (airline.get('protocol') or 'http').strip() or 'http'
    host = (airline.get('host') or 'localhost').strip() or 'localhost'
    port = str(airline.get('port', 8080)).strip() or '8080'
    base = (airline.get('basePath') or '/api').strip() or '/api'
    if not base.startswith('/'):
        base = '/' + base
    url = f"{protocol}://{host}:{port}{base}"
    return url.rstrip('/')


def build_airline_endpoint_url(base_url: str, endpoint: Optional[str]) -> str:
    endpoint = (endpoint or '').strip()
    if not endpoint:
        return base_url
    if not base_url.endswith('/'):
        base_url = base_url + '/'
    return base_url + endpoint.lstrip('/')


def get_airline_timeout_seconds_from_doc(airline: dict, default_ms: int = 40000) -> float:
    try:
        if 'timeoutMs' in airline and airline.get('timeoutMs') is not None:
            return max(3, int(airline.get('timeoutMs', default_ms)) // 1000)
    except Exception:
        pass
    return max(3, default_ms // 1000)


def get_airline_headers_for_doc(airline: dict) -> dict:
    headers = {'Content-Type': 'application/json'}
    api_key = (airline.get('apiKey') or '').strip()
    if api_key:
        headers['X-API-Key'] = api_key
    return headers


def fetch_airline_cities_for_doc(airline: dict, timeout: Optional[float] = None) -> list:
    base_url = build_airline_base_url_from_doc(airline)
    endpoints = airline.get('endpoints') or {}
    cities_endpoint = endpoints.get('cities') or 'airline/cities'
    url = build_airline_endpoint_url(base_url, cities_endpoint)
    headers = get_airline_headers_for_doc(airline)
    read_timeout = timeout or get_airline_timeout_seconds_from_doc(airline)
    try:
        response = requests.get(url, headers=headers, timeout=(3.0, read_timeout))
        response.raise_for_status()
        data = response.json()
        if isinstance(data, dict) and isinstance(data.get('data'), list):
            return data['data']
        if isinstance(data, list):
            return data
    except Exception as exc:
        print(f"❌ Error obteniendo ciudades de {airline.get('name')}: {exc}")
    return []


def resolve_city_id_for_airline(airline: dict, city_value: Optional[Union[str, int]], city_country: Optional[str], cities_cache: Optional[list] = None) -> Optional[str]:
    if city_value is None:
        return None
    value_str = str(city_value).strip()
    if not value_str:
        return None
    if value_str.isdigit():
        return value_str

    # Lazy load cities if not provided
    cities = cities_cache if cities_cache is not None else fetch_airline_cities_for_doc(airline)
    needle_name = value_str.lower()
    needle_country = (city_country or '').strip().lower()

    for city in cities:
        name = str(city.get('name') or '').strip().lower()
        country = str(city.get('country') or '').strip().lower()
        if name == needle_name and (not needle_country or not country or country == needle_country):
            for key in ('idCity', 'id_city', 'id', 'cityId', 'id_city_pk'):
                if key in city and city[key] is not None:
                    return str(city[key])
    return None


@csrf_exempt
def proxy_airline_cities(request):
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    data = safe_get(f"{base}/airline/cities")
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    items = data if isinstance(data, list) else data.get('data') or []
    return JsonResponse({ 'success': True, 'source': base, 'count': len(items), 'data': items })


@csrf_exempt
def proxy_airline_flights(request):
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    params = request.GET.dict()
    data = safe_get(f"{base}/airline/flights", params=params)
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    norm = normalize_flights_response(data)
    return JsonResponse(norm)


@csrf_exempt
def proxy_airline_seats(request):
    if request.method != 'GET':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    flight_id = (request.GET.get('flightId') or '').strip()
    if not flight_id:
        return JsonResponse({ 'success': False, 'message': 'flightId requerido' }, status=400)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    # Usar el endpoint de Aerolínea que devuelve seatsByCategory
    url = f"{base}/airline/flights/{flight_id}/seats"
    data = safe_get(url)
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    # Respuesta ya contiene { seatsByCategory, seatInfo, ... }
    return JsonResponse({ 'success': True, **({k:v for k,v in data.items()}) })


@csrf_exempt
def proxy_airline_create_ticket(request):
    if request.method != 'POST':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    try:
        payload = parse_request_data(request)
    except Exception:
        payload = {}
    data = safe_post(f"{base}/airline/tickets", json_body=payload)
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    # Passthrough
    if isinstance(data, dict):
        return JsonResponse(data)
    return JsonResponse({ 'success': True, 'data': data })


@csrf_exempt
def proxy_airline_create_roundtrip(request):
    """
    Crea 2 tickets para vuelo redondo (ida y vuelta).
    """
    if request.method != 'POST':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    try:
        payload = parse_request_data(request)
    except Exception:
        payload = {}
    
    print(f"🔄 Creando round-trip con payload: {payload}")
    data = safe_post(f"{base}/airline/tickets/round-trip", json_body=payload)
    
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    return JsonResponse(data)


@csrf_exempt
def proxy_airline_create_stopover(request):
    """
    Crea 2 tickets para vuelo con escala (2 segmentos).
    """
    if request.method != 'POST':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    try:
        payload = parse_request_data(request)
    except Exception:
        payload = {}
    
    print(f"✈️ Creando vuelo con escala: {payload}")
    data = safe_post(f"{base}/airline/tickets/with-stopover", json_body=payload)
    
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    return JsonResponse(data)


@csrf_exempt
def proxy_airline_login(request):
    if request.method != 'POST':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    payload = parse_request_data(request)
    headers = get_airline_headers()
    
    try:
        r = requests.post(f"{base}/airline/login", json=payload, headers=headers, timeout=(3.0, airline_timeout_seconds()))
        content_type = r.headers.get('content-type', 'application/json')
        if 'application/json' in content_type:
            try:
                body = r.json()
            except Exception:
                body = {}
            return JsonResponse(body, status=r.status_code)
        return HttpResponse(r.content, content_type=content_type, status=r.status_code)
    except Exception as e:
        return JsonResponse({ 'success': False, 'message': str(e) }, status=502)


@csrf_exempt
def proxy_airline_register(request):
    if request.method != 'POST':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    payload = parse_request_data(request)
    headers = get_airline_headers()
    
    try:
        r = requests.post(f"{base}/airline/register", json=payload, headers=headers, timeout=(3.0, airline_timeout_seconds()))
        content_type = r.headers.get('content-type', 'application/json')
        if 'application/json' in content_type:
            try:
                body = r.json()
            except Exception:
                body = {}
            return JsonResponse(body, status=r.status_code)
        return HttpResponse(r.content, content_type=content_type, status=r.status_code)
    except Exception as e:
        return JsonResponse({ 'success': False, 'message': str(e) }, status=502)


@csrf_exempt
def proxy_airline_tickets(request):
    # Listado de tickets con filtros (userId, flightId, status)
    if request.method != 'GET':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    params = request.GET.dict()
    data = safe_get(f"{base}/airline/tickets", params=params)
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    return JsonResponse(data)


@csrf_exempt
def proxy_airline_ticket_pdf(request, ticket_id: str):
    # Descarga/puente del PDF
    if request.method != 'GET':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    res = stream_get(f"{base}/airline/tickets/{ticket_id}/pdf")
    if isinstance(res, Exception):
        return JsonResponse({ 'success': False, 'message': str(res) }, status=502)
    content_type = res.headers.get('content-type', 'application/pdf')
    # Passthrough de status en caso no sea 200
    status = res.status_code
    return HttpResponse(res.content, content_type=content_type, status=status)


@csrf_exempt
def proxy_airline_ticket_by_id(request, ticket_id: str):
    if request.method != 'GET':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    data = safe_get(f"{base}/airline/tickets/{ticket_id}")
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    return JsonResponse(data)


@csrf_exempt
def proxy_airline_corporate_tickets(request):
    """
    Obtiene todos los tickets comprados por la agencia (usuario empresarial).
    Requiere que esté configurado el API_KEY en MongoDB.
    """
    if request.method != 'GET':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    # Endpoint especial para tickets empresariales
    data = safe_get(f"{base}/airline/tickets/corporate")
    
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    
    return JsonResponse(data)


@csrf_exempt
def proxy_airline_flight_reviews(request, flight_id: str):
    if request.method != 'GET':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    
    params = request.GET.dict()
    # forzar mode=tree si se solicita tree
    qs = {}
    qs.update(params)
    data = safe_get(f"{base}/airline/flights/{flight_id}/reviews", params=qs)
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    # normalizar campos para front
    try:
        def normalize_node(r):
            # El backend puede usar 'reviewText' para el comentario, y datos del usuario dentro de 'user'
            author = r.get('authorName') or ( (r.get('user') or {}).get('firstName', '') + ' ' + (r.get('user') or {}).get('lastName','') ).strip() or (r.get('user') or {}).get('name') or 'Usuario'
            children = r.get('children') or []
            return {
                'id': r.get('idReview') or r.get('id'),
                'authorName': author,
                'rating': r.get('rating') or 0,
                'comment': r.get('comment') or r.get('reviewText') or r.get('text') or '',
                'children': [ normalize_node(c) for c in children ]
            }
        items = data.get('reviews') if isinstance(data, dict) else data
        norm = [ normalize_node(r) for r in (items or []) ]
        return JsonResponse({ 'success': True, 'reviews': norm })
    except Exception:
        return JsonResponse(data if isinstance(data, dict) else { 'success': True, 'reviews': data })


# ================== REVIEWS PROPIAS DE LA AGENCIA (Mongo) ==================

def build_tree(items):
    by_id = {str(i.get('_id')): i for i in items}
    for it in items:
        it['id'] = str(it.get('_id'))
        it.pop('_id', None)
        it['children'] = []
    roots = []
    for it in items:
        pid = it.get('parentId')
        if pid and str(pid) in by_id:
            by_id[str(pid)]['children'].append(it)
        else:
            roots.append(it)
    return roots


@csrf_exempt
def agency_reviews_view(request):
    db = get_db()
    col = db.get_collection('agency_reviews')
    if request.method == 'POST':
        try:
            data = parse_request_data(request)
            doc = {
                'flightId': data.get('flightId'),
                'ticketId': data.get('ticketId'),
                'parentId': data.get('parentId'),
                'authorName': (data.get('authorName') or 'Usuario'),
                'rating': int(data.get('rating') or 0),
                'comment': (data.get('comment') or '').strip(),
                'createdAt': datetime.utcnow(),
            }
            res = col.insert_one(doc)
            out = {
                'id': str(res.inserted_id),
                'flightId': doc.get('flightId'),
                'ticketId': doc.get('ticketId'),
                'parentId': doc.get('parentId'),
                'authorName': doc.get('authorName'),
                'rating': doc.get('rating'),
                'comment': doc.get('comment'),
                'createdAt': doc['createdAt'].isoformat() + 'Z',
            }
            return JsonResponse({ 'success': True, 'data': out }, status=201)
        except Exception as e:
            return JsonResponse({ 'success': False, 'message': f'Error creando comentario: {type(e).__name__}' }, status=500)
    if request.method == 'GET':
        q = {}
        if request.GET.get('flightId'):
            q['flightId'] = request.GET.get('flightId')
        if request.GET.get('ticketId'):
            q['ticketId'] = request.GET.get('ticketId')
        cur = col.find(q).sort('createdAt', 1)
        items = list(cur)
        for it in items:
            it['_id'] = str(it['_id'])
        mode = (request.GET.get('mode') or '').strip().lower()
        if mode == 'tree':
            tree = build_tree(items)
            return JsonResponse({ 'success': True, 'reviews': tree, 'mode': 'tree' })
        return JsonResponse({ 'success': True, 'reviews': items, 'mode': 'flat' })
    return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)


# --------- Debug: información de conexión a Mongo ---------
def db_info_view(request):
    try:
        db = get_db()
        cols = sorted(db.list_collection_names())
        uri = getattr(settings, 'MONGODB_URI', '')
        safe_uri = uri.split('@')[-1] if '@' in uri else uri
        return JsonResponse({
            'success': True,
            'database': db.name,
            'collections': cols,
            'uri_hint': safe_uri,
        })
    except Exception as e:
        return JsonResponse({ 'success': False, 'error': str(e) }, status=500)

@csrf_exempt
def users_list_view(request):
    db = get_db()
    if request.method == 'GET':
        q = (request.GET.get('q') or '').strip().lower()
        try:
            page = max(1, int(request.GET.get('page', '1')))
            page_size = min(100, max(1, int(request.GET.get('pageSize', '20'))))
        except Exception:
            page, page_size = 1, 20
        query = {}
        if q:
            query['$or'] = [
                {'firstName': {'$regex': q, '$options': 'i'}},
                {'lastName': {'$regex': q, '$options': 'i'}},
                {'email': {'$regex': q, '$options': 'i'}},
            ]
        total = db.users.count_documents(query)
        cursor = db.users.find(query).sort('createdAt', -1).skip((page-1)*page_size).limit(page_size)
        users = list(cursor)
        out = []
        for u in users:
            out.append({
                'id': str(u.get('_id')),
                'firstName': u.get('firstName',''),
                'lastName': u.get('lastName',''),
                'email': u.get('email',''),
                'age': u.get('age'),
                'country': u.get('country'),
                'passportNumber': u.get('passportNumber'),
                'phone': u.get('phone'),
                'address': u.get('address'),
                'role': u.get('role','user'),
                'isActive': u.get('isActive', True),
                'createdAt': u.get('createdAt'),
                'updatedAt': u.get('updatedAt'),
            })
        return JsonResponse({ 'success': True, 'data': out, 'page': page, 'pageSize': page_size, 'total': total })
    if request.method == 'POST':
        try:
            data = parse_request_data(request)
            # Reutilizamos validaciones del registro, pero permitimos role/isActive
            firstName = (data.get('firstName') or '').strip()
            lastName = (data.get('lastName') or '').strip()
            email = (data.get('email') or '').strip().lower()
            password = data.get('password') or ''
            age = int(data.get('age') or 18)
            country = (data.get('country') or '').strip()
            passportNumber = (data.get('passportNumber') or '').strip()
            phone = (data.get('phone') or '').strip()
            address = (data.get('address') or '').strip()
            role = (data.get('role') or 'user').strip()
            isActive = bool(data.get('isActive', True))
            if not firstName or not lastName or not email or not email_pattern.match(email):
                return JsonResponse({'success': False, 'message': 'Datos inválidos'}, status=400)
            if not password or len(password) < 8:
                return JsonResponse({'success': False, 'message': 'Contraseña inválida'}, status=400)
            if db.users.find_one({'email': email}):
                return JsonResponse({'success': False, 'message': 'Email duplicado'}, status=400)
            salt = bcrypt.gensalt(rounds=12)
            password_hash = bcrypt.hashpw(password.encode('utf-8'), salt).decode('utf-8')
            doc = {
                'firstName': firstName, 'lastName': lastName, 'email': email,
                'password': password_hash, 'age': age, 'country': country,
                'passportNumber': passportNumber, 'phone': phone or None,
                'address': address or None, 'role': role, 'isActive': isActive,
                'createdAt': datetime.utcnow(), 'updatedAt': datetime.utcnow(),
            }
            res = db.users.insert_one(doc)
            doc['id'] = str(res.inserted_id)
            doc.pop('password', None)
            return JsonResponse({'success': True, 'data': doc}, status=201)
        except Exception:
            return JsonResponse({'success': False, 'message': 'Error al crear usuario'}, status=500)
    return JsonResponse({'success': False, 'message': 'Método no permitido'}, status=405)


@csrf_exempt
def users_detail_view(request, user_id: str):
    from bson import ObjectId
    db = get_db()
    try:
        oid = ObjectId(user_id)
    except Exception:
        return JsonResponse({'success': False, 'message': 'ID inválido'}, status=400)

    if request.method == 'GET':
        u = db.users.find_one({'_id': oid})
        if not u:
            return JsonResponse({'success': False, 'message': 'No encontrado'}, status=404)
        out = {
            'id': str(u.get('_id')),
            'firstName': u.get('firstName',''),
            'lastName': u.get('lastName',''),
            'email': u.get('email',''),
            'age': u.get('age'),
            'country': u.get('country'),
            'passportNumber': u.get('passportNumber'),
            'phone': u.get('phone'),
            'address': u.get('address'),
            'role': u.get('role','user'),
            'isActive': u.get('isActive', True),
            'createdAt': u.get('createdAt'),
            'updatedAt': u.get('updatedAt'),
        }
        return JsonResponse({'success': True, 'data': out})
    if request.method in ['PATCH', 'PUT']:
        data = parse_request_data(request)
        updates = {}
        for key in ['firstName','lastName','age','country','passportNumber','phone','address','role','isActive']:
            if key in data:
                updates[key] = data[key]
        if 'email' in data:
            new_email = (data.get('email') or '').strip().lower()
            if not email_pattern.match(new_email):
                return JsonResponse({'success': False, 'message': 'Email inválido'}, status=400)
            if db.users.find_one({'email': new_email, '_id': {'$ne': oid}}):
                return JsonResponse({'success': False, 'message': 'Email ya usado'}, status=400)
            updates['email'] = new_email
        if 'password' in data:
            pwd = data.get('password') or ''
            if len(pwd) < 8:
                return JsonResponse({'success': False, 'message': 'Contraseña inválida'}, status=400)
            salt = bcrypt.gensalt(rounds=12)
            updates['password'] = bcrypt.hashpw(pwd.encode('utf-8'), salt).decode('utf-8')
        if not updates:
            return JsonResponse({'success': False, 'message': 'Nada para actualizar'}, status=400)
        updates['updatedAt'] = datetime.utcnow()
        db.users.update_one({'_id': oid}, {'$set': updates})
        return JsonResponse({'success': True})
    if request.method == 'DELETE':
        db.users.delete_one({'_id': oid})
        return JsonResponse({'success': True})
    return JsonResponse({'success': False, 'message': 'Método no permitido'}, status=405)


# ============================================================
# CORPORATE USERS (Usuarios Empresariales con API Keys)
# ============================================================

def generate_api_key():
    """Genera un API Key único de 32 caracteres"""
    import secrets
    return secrets.token_urlsafe(32)[:32]


@csrf_exempt
def corporate_users_list_view(request):
    """GET: Lista todos los usuarios corporativos | POST: Crea uno nuevo"""
    db = get_db()
    
    if request.method == 'GET':
        users = list(db.corporate_config.find())
        result = []
        for u in users:
            result.append({
                'idUser': str(u.get('_id')),
                'companyName': u.get('companyName', ''),
                'name': u.get('name', ''),
                'email': u.get('email', ''),
                'phone': u.get('phone', ''),
                'cui': u.get('cui', ''),
                'birthDate': u.get('birthDate'),
                'address': u.get('address', ''),
                'apiKey': u.get('apiKey', ''),
                'enabled': 1 if u.get('enabled', True) else 0,
                'createdAt': u.get('createdAt'),
                'updatedAt': u.get('updatedAt'),
            })
        return JsonResponse(result, safe=False)
    
    if request.method == 'POST':
        data = parse_request_data(request)
        
        # Validaciones
        required = ['companyName', 'name', 'email', 'phone', 'cui', 'address', 'password']
        for field in required:
            if not data.get(field):
                return JsonResponse({'error': f'Campo requerido: {field}'}, status=400)
        
        email = data.get('email', '').strip().lower()
        if not email_pattern.match(email):
            return JsonResponse({'error': 'Email inválido'}, status=400)
        
        # Verificar que no exista otro usuario con el mismo email
        if db.corporate_config.find_one({'email': email}):
            return JsonResponse({'error': 'Ya existe un usuario con este email'}, status=400)
        
        # Generar API Key único
        api_key = generate_api_key()
        while db.corporate_config.find_one({'apiKey': api_key}):
            api_key = generate_api_key()
        
        # Hashear contraseña
        pwd = data.get('password', '')
        if len(pwd) < 6:
            return JsonResponse({'error': 'La contraseña debe tener al menos 6 caracteres'}, status=400)
        
        salt = bcrypt.gensalt(rounds=12)
        hashed_password = bcrypt.hashpw(pwd.encode('utf-8'), salt).decode('utf-8')
        
        # Crear documento
        now = datetime.utcnow()
        doc = {
            'companyName': data.get('companyName', '').strip(),
            'name': data.get('name', '').strip(),
            'email': email,
            'phone': data.get('phone', '').strip(),
            'cui': data.get('cui', '').strip(),
            'birthDate': data.get('birthDate') or None,
            'address': data.get('address', '').strip(),
            'password': hashed_password,
            'apiKey': api_key,
            'enabled': True,
            'createdAt': now,
            'updatedAt': now,
        }
        
        result = db.corporate_config.insert_one(doc)
        
        return JsonResponse({
            'success': True,
            'idUser': str(result.inserted_id),
            'apiKey': api_key,
            'message': 'Usuario corporativo creado exitosamente'
        }, status=201)
    
    return JsonResponse({'error': 'Método no permitido'}, status=405)


@csrf_exempt
def corporate_users_detail_view(request, user_id: str):
    """GET: Obtiene un usuario | PUT: Actualiza un usuario"""
    from bson import ObjectId
    db = get_db()
    
    try:
        oid = ObjectId(user_id)
    except Exception:
        return JsonResponse({'error': 'ID inválido'}, status=400)
    
    user = db.corporate_config.find_one({'_id': oid})
    if not user:
        return JsonResponse({'error': 'Usuario no encontrado'}, status=404)
    
    if request.method == 'GET':
        return JsonResponse({
            'idUser': str(user.get('_id')),
            'companyName': user.get('companyName', ''),
            'name': user.get('name', ''),
            'email': user.get('email', ''),
            'phone': user.get('phone', ''),
            'cui': user.get('cui', ''),
            'birthDate': user.get('birthDate'),
            'address': user.get('address', ''),
            'apiKey': user.get('apiKey', ''),
            'enabled': 1 if user.get('enabled', True) else 0,
            'createdAt': user.get('createdAt'),
            'updatedAt': user.get('updatedAt'),
        })
    
    if request.method == 'PUT':
        data = parse_request_data(request)
        updates = {}
        
        # Campos actualizables
        for field in ['companyName', 'name', 'email', 'phone', 'cui', 'birthDate', 'address']:
            if field in data and data[field] is not None:
                if field == 'email':
                    email = data[field].strip().lower()
                    if not email_pattern.match(email):
                        return JsonResponse({'error': 'Email inválido'}, status=400)
                    # Verificar que no exista otro usuario con el mismo email
                    existing = db.corporate_config.find_one({'email': email, '_id': {'$ne': oid}})
                    if existing:
                        return JsonResponse({'error': 'Ya existe un usuario con este email'}, status=400)
                    updates[field] = email
                else:
                    updates[field] = data[field]
        
        # Actualizar enabled
        if 'enabled' in data:
            updates['enabled'] = bool(data['enabled'])
        
        # Actualizar contraseña si se proporciona
        if 'password' in data and data['password']:
            pwd = data['password']
            if len(pwd) < 6:
                return JsonResponse({'error': 'La contraseña debe tener al menos 6 caracteres'}, status=400)
            salt = bcrypt.gensalt(rounds=12)
            updates['password'] = bcrypt.hashpw(pwd.encode('utf-8'), salt).decode('utf-8')
        
        if not updates:
            return JsonResponse({'error': 'No hay datos para actualizar'}, status=400)
        
        updates['updatedAt'] = datetime.utcnow()
        
        db.corporate_config.update_one({'_id': oid}, {'$set': updates})
        
        return JsonResponse({
            'success': True,
            'message': 'Usuario actualizado exitosamente'
        })
    
    return JsonResponse({'error': 'Método no permitido'}, status=405)


@csrf_exempt
def corporate_users_regenerate_key_view(request, user_id: str):
    """PUT: Regenera el API Key de un usuario corporativo"""
    from bson import ObjectId
    db = get_db()
    
    if request.method != 'PUT':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    try:
        oid = ObjectId(user_id)
    except Exception:
        return JsonResponse({'error': 'ID inválido'}, status=400)
    
    user = db.corporate_config.find_one({'_id': oid})
    if not user:
        return JsonResponse({'error': 'Usuario no encontrado'}, status=404)
    
    # Generar nuevo API Key único
    api_key = generate_api_key()
    while db.corporate_config.find_one({'apiKey': api_key}):
        api_key = generate_api_key()
    
    db.corporate_config.update_one(
        {'_id': oid},
        {'$set': {'apiKey': api_key, 'updatedAt': datetime.utcnow()}}
    )
    
    return JsonResponse({
        'success': True,
        'apiKey': api_key,
        'message': 'API Key regenerado exitosamente'
    })


@csrf_exempt
def corporate_users_toggle_status_view(request, user_id: str):
    """PUT: Activa/desactiva un usuario corporativo"""
    from bson import ObjectId
    db = get_db()
    
    if request.method != 'PUT':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    try:
        oid = ObjectId(user_id)
    except Exception:
        return JsonResponse({'error': 'ID inválido'}, status=400)
    
    user = db.corporate_config.find_one({'_id': oid})
    if not user:
        return JsonResponse({'error': 'Usuario no encontrado'}, status=404)
    
    new_status = not user.get('enabled', True)
    
    db.corporate_config.update_one(
        {'_id': oid},
        {'$set': {'enabled': new_status, 'updatedAt': datetime.utcnow()}}
    )
    
    return JsonResponse({
        'success': True,
        'enabled': new_status,
        'message': f'Usuario {"activado" if new_status else "desactivado"} exitosamente'
    })


# ============================================================================
# SERVICIO AGREGADOR DE VUELOS (Multi-Aerolíneas)
# ============================================================================

@csrf_exempt
def aggregated_flight_search(request):
    """
    Busca vuelos en todas las aerolíneas configuradas en paralelo.
    GET /api/flights/aggregated-search?originName=Guatemala&destinationName=Panama&departureDate=2025-10-29
    """

    if request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    query = request.GET

    origin_value = (query.get('originName') or query.get('origin') or query.get('originCity') or '').strip()
    origin_country = (query.get('originCountry') or query.get('origin_country') or '').strip()
    destination_value = (query.get('destinationName') or query.get('destination') or query.get('destinationCity') or '').strip()
    destination_country = (query.get('destinationCountry') or query.get('destination_country') or '').strip()
    departure_date = (query.get('departureDate') or query.get('date') or '').strip()
    return_date = (query.get('returnDate') or query.get('return') or '').strip()
    passengers = (query.get('passengers') or query.get('travelers') or '1').strip() or '1'
    seat_category = (query.get('seatCategory') or query.get('seat_class') or query.get('cabin') or '').strip()
    nonstop = (query.get('nonstop') or '').strip()

    if not origin_value or not destination_value or not departure_date:
        return JsonResponse({
            'error': 'Parámetros requeridos: originName, destinationName, departureDate'
        }, status=400)
    
    db = get_db()
    airlines = list(db.airlines.find({'enabled': True}))
    
    if not airlines:
        return JsonResponse({
            'success': True,
            'totalFlights': 0,
            'airlinesQueried': 0,
            'flights': [],
            'message': 'No hay aerolíneas configuradas'
        })
    
    aggregated_flights = []
    airlines_summary = []
    airlines_with_results = 0
    airlines_with_errors = []
    
    def price_value(flight: dict) -> float:
        for key in ('basePrice', 'price', 'fare', 'totalAmount'):
            value = flight.get(key)
            if isinstance(value, Decimal):
                return float(value)
            try:
                if value is not None:
                    return float(value)
            except (TypeError, ValueError):
                continue
        return float('inf')

    def query_airline(airline_doc: dict) -> dict:
        airline_id = str(airline_doc.get('_id'))
        base_url = build_airline_base_url_from_doc(airline_doc)
        headers = get_airline_headers_for_doc(airline_doc)
        timeout = get_airline_timeout_seconds_from_doc(airline_doc)

        cities = fetch_airline_cities_for_doc(airline_doc, timeout=timeout)
        origin_id = resolve_city_id_for_airline(airline_doc, origin_value, origin_country, cities) if origin_value else None
        destination_id = resolve_city_id_for_airline(airline_doc, destination_value, destination_country, cities) if destination_value else None

        if origin_value and origin_id is None:
            return {
                'error': f"La ciudad de origen '{origin_value}' no está disponible en {airline_doc.get('name')}",
                'airlineCities': cities,
                'flights': [],
                'baseUrl': base_url,
                'endpoint': None
            }

        if destination_value and destination_id is None:
            return {
                'error': f"La ciudad de destino '{destination_value}' no está disponible en {airline_doc.get('name')}",
                'airlineCities': cities,
                'flights': [],
                'baseUrl': base_url,
                'endpoint': None
            }

        params = {}
        if origin_id:
            params['origin'] = origin_id
        if destination_id:
            params['destination'] = destination_id
        if departure_date:
            params['departureDate'] = departure_date
        if return_date:
            params['returnDate'] = return_date
        if passengers:
            params['passengers'] = passengers
        if seat_category:
            params['seatCategory'] = seat_category
        if nonstop:
            params['nonstop'] = nonstop

        endpoints = airline_doc.get('endpoints') or {}
        search_endpoint = endpoints.get('search') or 'airline/flights'
        url = build_airline_endpoint_url(base_url, search_endpoint)

        response = requests.get(
            url,
            params=params,
            headers=headers,
            timeout=(3.0, timeout)
        )
        response.raise_for_status()

        payload = response.json() if response.headers.get('content-type', '').startswith('application/json') else {}

        flights_payload = []
        if isinstance(payload, dict):
            if isinstance(payload.get('flights'), list):
                flights_payload = payload.get('flights')
            elif isinstance(payload.get('data'), list):
                flights_payload = payload.get('data')
        elif isinstance(payload, list):
            flights_payload = payload

        enriched_flights = []
        for flight in flights_payload:
            if not isinstance(flight, dict):
                continue
            flight_copy = dict(flight)
            price = flight_copy.get('basePrice')
            if isinstance(price, Decimal):
                flight_copy['basePrice'] = float(price)
            elif isinstance(price, str):
                try:
                    flight_copy['basePrice'] = float(price)
                except ValueError:
                    pass
            flight_copy['airlineId'] = airline_id
            flight_copy['airlineName'] = airline_doc.get('name')
            flight_copy['airlineCode'] = airline_doc.get('code')
            flight_copy['airlineBaseUrl'] = base_url
            flight_copy['airlineEndpoint'] = search_endpoint
            unique_parts = [airline_doc.get('code') or airline_doc.get('name') or 'AL', str(flight_copy.get('idFlight') or flight_copy.get('id') or flight_copy.get('flightNumber') or flight_copy.get('flightId') or '')]
            flight_copy['uniqueId'] = '-'.join([p for p in unique_parts if p])
            flight_copy['priceValue'] = price_value(flight_copy)
            enriched_flights.append(flight_copy)

        return {
            'flights': enriched_flights,
            'airlineCities': cities,
            'baseUrl': base_url,
            'endpoint': url,
            'raw': payload,
        }

    with ThreadPoolExecutor(max_workers=min(8, len(airlines))) as executor:
        future_map = {executor.submit(query_airline, airline): airline for airline in airlines}
        for future in as_completed(future_map):
            airline_doc = future_map[future]
            airline_id = str(airline_doc.get('_id'))
            airline_name = airline_doc.get('name')
            summary_entry = {
                'id': airline_id,
                'name': airline_name,
                'code': airline_doc.get('code'),
                'baseUrl': build_airline_base_url_from_doc(airline_doc),
                'timeoutSeconds': get_airline_timeout_seconds_from_doc(airline_doc),
                'flightsFound': 0,
            }
            try:
                result = future.result()
                error = result.get('error')
                flights = result.get('flights') or []
                summary_entry['flightsFound'] = len(flights)
                if error:
                    summary_entry['error'] = error
                    airlines_with_errors.append({'id': airline_id, 'name': airline_name, 'error': error})
                else:
                    if flights:
                        airlines_with_results += 1
                        aggregated_flights.extend(flights)
            except requests.exceptions.Timeout:
                summary_entry['error'] = 'Timeout'
                airlines_with_errors.append({'id': airline_id, 'name': airline_name, 'error': 'Timeout - La aerolínea no respondió a tiempo'})
            except requests.exceptions.ConnectionError:
                summary_entry['error'] = 'ConnectionError'
                airlines_with_errors.append({'id': airline_id, 'name': airline_name, 'error': 'Error de conexión con la aerolínea'})
            except Exception as exc:
                summary_entry['error'] = str(exc)
                airlines_with_errors.append({'id': airline_id, 'name': airline_name, 'error': str(exc)})
            airlines_summary.append(summary_entry)

    aggregated_flights.sort(key=lambda x: x.get('priceValue', float('inf')))
    for flight in aggregated_flights:
        flight.pop('priceValue', None)
    
    response_payload = {
        'success': True,
        'totalFlights': len(aggregated_flights),
        'airlinesQueried': len(airlines),
        'airlinesWithResults': airlines_with_results,
        'flights': aggregated_flights,
        'airlinesSummary': airlines_summary,
        'searchParams': {
            'originName': origin_value,
            'originCountry': origin_country,
            'destinationName': destination_value,
            'destinationCountry': destination_country,
            'departureDate': departure_date,
            'returnDate': return_date,
            'passengers': passengers,
            'seatCategory': seat_category,
            'nonstop': nonstop or None,
        }
    }

    if airlines_with_errors:
        response_payload['airlinesWithErrors'] = airlines_with_errors

    return JsonResponse(response_payload)


@csrf_exempt
def aggregated_airline_cities(request):
    if request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido'}, status=405)

    db = get_db()
    airlines = list(db.airlines.find({'enabled': True}))

    if not airlines:
        return JsonResponse({
            'success': True,
            'totalAirlines': 0,
            'totalCities': 0,
            'cities': [],
            'flat': []
        })

    def extract_city_id(city_doc: dict) -> Optional[str]:
        for key in ('idCity', 'id_city', 'id', 'cityId', 'city_id', 'id_city_pk'):
            if key in city_doc and city_doc[key] is not None and str(city_doc[key]).strip():
                return str(city_doc[key]).strip()
        return None

    flat_entries = []
    grouped_map: dict[str, list] = {}
    errors = []

    def collect_cities(airline_doc: dict) -> tuple[str, list]:
        timeout = get_airline_timeout_seconds_from_doc(airline_doc)
        cities = fetch_airline_cities_for_doc(airline_doc, timeout=timeout)
        return str(airline_doc.get('_id')), cities

    with ThreadPoolExecutor(max_workers=min(8, len(airlines))) as executor:
        futures = {executor.submit(collect_cities, airline): airline for airline in airlines}
        for future in as_completed(futures):
            airline_doc = futures[future]
            airline_id = str(airline_doc.get('_id'))
            airline_name = airline_doc.get('name')
            airline_code = airline_doc.get('code')
            try:
                _, cities = future.result()
                if not cities:
                    errors.append({'id': airline_id, 'name': airline_name, 'error': 'Sin ciudades o no disponible'})
                for city in cities:
                    city_id = extract_city_id(city) or ''
                    entry = {
                        'airlineId': airline_id,
                        'airlineName': airline_name,
                        'airlineCode': airline_code,
                        'cityId': city_id,
                        'name': city.get('name'),
                        'country': city.get('country'),
                        'raw': city
                    }
                    flat_entries.append(entry)
                    grouped_key = f"{(city.get('name') or '').strip().lower()}|{(city.get('country') or '').strip().lower()}"
                    grouped_map.setdefault(grouped_key, []).append(entry)
            except Exception as exc:
                errors.append({'id': airline_id, 'name': airline_name, 'error': str(exc)})

    grouped_cities = []
    for group_entries in grouped_map.values():
        if not group_entries:
            continue
        primary = group_entries[0]
        label = primary.get('name') or 'Ciudad'
        country = primary.get('country')
        if country:
            label = f"{label} ({country})"
        grouped_cities.append({
            'label': label,
            'name': primary.get('name'),
            'country': country,
            'airlines': group_entries
        })

    grouped_cities.sort(key=lambda item: item.get('label') or '')

    payload = {
        'success': True,
        'totalAirlines': len(airlines),
        'totalCities': len(flat_entries),
        'cities': grouped_cities,
        'flat': flat_entries,
    }

    if errors:
        payload['errors'] = errors

    return JsonResponse(payload)


@csrf_exempt
def aggregated_all_flights(request):
    """Obtiene todos los vuelos de todas las aerolíneas activas sin filtros y deja el filtrado al frontend."""
    if request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido'}, status=405)

    db = get_db()
    airlines = list(db.airlines.find({'enabled': True}))

    if not airlines:
        return JsonResponse({
            'success': True,
            'totalFlights': 0,
            'airlinesQueried': 0,
            'flights': [],
            'airlinesSummary': [],
            'airlinesWithErrors': []
        })

    aggregated_flights = []
    airlines_summary = []
    airlines_with_errors = []

    def fetch_all_flights_for_airline(airline_doc: dict) -> list:
        airline_id = str(airline_doc.get('_id'))
        base_url = build_airline_base_url_from_doc(airline_doc)
        endpoints = airline_doc.get('endpoints') or {}
        flights_endpoint = endpoints.get('flights') or 'airline/flights'
        url = build_airline_endpoint_url(base_url, flights_endpoint)
        headers = get_airline_headers_for_doc(airline_doc)
        timeout = get_airline_timeout_seconds_from_doc(airline_doc)

        response = requests.get(url, headers=headers, timeout=(3.0, timeout))
        response.raise_for_status()

        payload = response.json() if response.headers.get('content-type', '').startswith('application/json') else {}
        flights_payload: List[Dict[str, Any]] = []
        if isinstance(payload, dict):
            if isinstance(payload.get('flights'), list):
                flights_payload = payload.get('flights')
            elif isinstance(payload.get('data'), list):
                flights_payload = payload.get('data')
        elif isinstance(payload, list):
            flights_payload = payload

        enriched: List[Dict[str, Any]] = []
        for flight in flights_payload:
            if not isinstance(flight, dict):
                continue
            flight_copy = dict(flight)
            price = flight_copy.get('basePrice')
            if isinstance(price, Decimal):
                flight_copy['basePrice'] = float(price)
            elif isinstance(price, str):
                try:
                    flight_copy['basePrice'] = float(price)
                except ValueError:
                    pass

            flight_copy['airlineId'] = airline_id
            flight_copy['airlineName'] = airline_doc.get('name')
            flight_copy['airlineCode'] = airline_doc.get('code')
            flight_copy['airlineBaseUrl'] = base_url
            flight_copy['airlineEndpoint'] = flights_endpoint
            unique_parts = [airline_doc.get('code') or airline_doc.get('name') or 'AL', str(flight_copy.get('idFlight') or flight_copy.get('id') or flight_copy.get('flightNumber') or flight_copy.get('flightId') or '')]
            flight_copy['uniqueId'] = '-'.join([part for part in unique_parts if part])
            enriched.append(flight_copy)

        return enriched

    with ThreadPoolExecutor(max_workers=min(8, len(airlines))) as executor:
        future_map = {executor.submit(fetch_all_flights_for_airline, airline): airline for airline in airlines}
        for future in as_completed(future_map):
            airline_doc = future_map[future]
            airline_id = str(airline_doc.get('_id'))
            summary_entry = {
                'id': airline_id,
                'name': airline_doc.get('name'),
                'code': airline_doc.get('code'),
                'flightsFound': 0,
            }
            try:
                flights = future.result()
                summary_entry['flightsFound'] = len(flights)
                aggregated_flights.extend(flights)
            except requests.exceptions.Timeout:
                summary_entry['error'] = 'Timeout'
                airlines_with_errors.append({'id': airline_id, 'name': airline_doc.get('name'), 'error': 'Timeout - La aerolínea no respondió a tiempo'})
            except requests.exceptions.ConnectionError:
                summary_entry['error'] = 'ConnectionError'
                airlines_with_errors.append({'id': airline_id, 'name': airline_doc.get('name'), 'error': 'Error de conexión con la aerolínea'})
            except Exception as exc:
                summary_entry['error'] = str(exc)
                airlines_with_errors.append({'id': airline_id, 'name': airline_doc.get('name'), 'error': str(exc)})
            airlines_summary.append(summary_entry)

    return JsonResponse({
        'success': True,
        'totalFlights': len(aggregated_flights),
        'airlinesQueried': len(airlines),
        'flights': aggregated_flights,
        'airlinesSummary': airlines_summary,
        'airlinesWithErrors': airlines_with_errors,
    })


@csrf_exempt
def aggregated_flight_purchase(request):
    """
    Compra un vuelo en una aerolínea específica
    POST /api/flights/aggregated-purchase
    Body: { airlineId, flightId, ...purchaseData }
    """
    if request.method != 'POST':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    try:
        data = parse_request_data(request)
        airline_id = data.get('airlineId')
        flight_id = data.get('flightId')
        
        if not airline_id or not flight_id:
            return JsonResponse({
                'error': 'airlineId y flightId son requeridos'
            }, status=400)
        
        db = get_db()
        try:
            airline_oid = ObjectId(airline_id)
        except Exception:
            return JsonResponse({'error': 'airlineId inválido'}, status=400)
        
        airline = db.airlines.find_one({'_id': airline_oid})
        if not airline:
            return JsonResponse({'error': 'Aerolínea no encontrada'}, status=404)
        
        if not airline.get('enabled', False):
            return JsonResponse({'error': 'Aerolínea deshabilitada'}, status=400)
        
        base_url = build_airline_base_url_from_doc(airline)
        endpoints = airline.get('endpoints') or {}
        book_endpoint = endpoints.get('book') or 'airline/tickets'
        
        # Si el endpoint configurado es solo '/book', usar 'airline/tickets'
        if book_endpoint in ('/book', 'book'):
            book_endpoint = 'airline/tickets'
            print(f"⚠️  Endpoint '/book' detectado, usando 'airline/tickets' en su lugar")
        
        full_url = build_airline_endpoint_url(base_url, book_endpoint)

        headers = get_airline_headers_for_doc(airline)

        purchase_payload = {
            'flightId': flight_id,
            **{k: v for k, v in data.items() if k != 'airlineId'}
        }
        
        print(f"💳 Comprando vuelo en {airline.get('name')}: {full_url}")
        print(f"🔑 API Key: {headers.get('X-API-Key', 'NO CONFIGURADO')[:15]}...")
        print(f"📦 Payload: {purchase_payload}")
        
        timeout = get_airline_timeout_seconds_from_doc(airline, default_ms=20000)
        response = requests.post(
            full_url,
            json=purchase_payload,
            headers=headers,
            timeout=(3.0, timeout)
        )
        
        if response.status_code in (200, 201):
            result = response.json() if response.headers.get('content-type', '').startswith('application/json') else {'success': True}
            result['airlineName'] = airline.get('name')
            result['airlineCode'] = airline.get('code')
            print(f"✅ Compra exitosa en {airline.get('name')}")
            return JsonResponse(result)

        error_payload = response.json() if response.headers.get('content-type', '').startswith('application/json') else {'error': response.text}
        print(f"❌ Error en compra: {response.status_code} - {error_payload}")
        return JsonResponse({
            'error': f"Error en compra con {airline.get('name')}",
            'details': error_payload,
            'statusCode': response.status_code
        }, status=response.status_code)
            
    except requests.exceptions.Timeout:
        return JsonResponse({
            'error': 'Timeout - La aerolínea no respondió a tiempo'
        }, status=504)
    except requests.exceptions.ConnectionError:
        return JsonResponse({
            'error': 'Error de conexión con la aerolínea'
        }, status=503)
    except Exception as e:
        print(f"❌ Error en compra agregada: {str(e)}")
        return JsonResponse({
            'error': 'Error interno al procesar la compra',
            'details': str(e)
        }, status=500)


@csrf_exempt
def aggregated_flight_seats(request):
    if request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido'}, status=405)

    airline_id = (request.GET.get('airlineId') or '').strip()
    flight_id = (request.GET.get('flightId') or '').strip()

    if not airline_id or not flight_id:
        return JsonResponse({'error': 'Parámetros requeridos: airlineId y flightId'}, status=400)

    db = get_db()
    try:
        airline_oid = ObjectId(airline_id)
    except Exception:
        return JsonResponse({'error': 'airlineId inválido'}, status=400)

    airline = db.airlines.find_one({'_id': airline_oid})
    if not airline:
        return JsonResponse({'error': 'Aerolínea no encontrada'}, status=404)

    if not airline.get('enabled', False):
        return JsonResponse({'error': 'Aerolínea deshabilitada'}, status=400)

    base_url = build_airline_base_url_from_doc(airline)
    endpoints = airline.get('endpoints') or {}
    seats_endpoint_template = endpoints.get('seats') or 'airline/flights/{flightId}/seats'
    if '{flightId}' in seats_endpoint_template:
        endpoint_path = seats_endpoint_template.replace('{flightId}', str(flight_id))
    else:
        endpoint_path = seats_endpoint_template.rstrip('/') + f'/{flight_id}'
        if not endpoint_path.endswith('/seats'):
            endpoint_path = endpoint_path.rstrip('/') + '/seats'

    url = build_airline_endpoint_url(base_url, endpoint_path)
    headers = get_airline_headers_for_doc(airline)
    timeout = get_airline_timeout_seconds_from_doc(airline)

    try:
        response = requests.get(url, headers=headers, timeout=(3.0, timeout))
        response.raise_for_status()
        if response.headers.get('content-type', '').startswith('application/json'):
            payload = response.json()
        else:
            payload = {'success': True, 'raw': response.text}
        payload.setdefault('success', True)
        payload.setdefault('airlineName', airline.get('name'))
        payload.setdefault('airlineCode', airline.get('code'))
        return JsonResponse(payload)
    except requests.exceptions.Timeout:
        return JsonResponse({'error': 'Timeout - La aerolínea no respondió a tiempo'}, status=504)
    except requests.exceptions.ConnectionError:
        return JsonResponse({'error': 'Error de conexión con la aerolínea'}, status=503)
    except Exception as exc:
        return JsonResponse({'error': str(exc)}, status=502)


# ============================================================================
# CONFIGURACIÓN DEL SITIO
# ============================================================================

@csrf_exempt
def site_config_view(request):
    """
    GET: Obtiene la configuración del sitio (nombre de la agencia, footer, etc.)
    PUT: Actualiza la configuración del sitio
    """
    db = get_db()
    config_collection = db['site_config']
    
    if request.method == 'GET':
        config = config_collection.find_one()
        if not config:
            # Configuración por defecto si no existe
            default_config = {
                'agencyName': 'Agencia de Viajes',
                'footer': {
                    'companyName': 'Agencia de Viajes S.A.',
                    'address': 'Av. Principal 123, Ciudad',
                    'phone': '+1 234 567 890',
                    'email': 'info@agencia.com',
                    'socialMedia': {
                        'facebook': '',
                        'twitter': '',
                        'instagram': ''
                    }
                },
                'logo': '',
                'primaryColor': '#1e40af',
                'secondaryColor': '#3b82f6'
            }
            config_collection.insert_one(default_config)
            config = default_config
        
        if '_id' in config:
            config['_id'] = str(config['_id'])
        
        return JsonResponse({'success': True, 'config': config})
    
    elif request.method == 'PUT':
        # Verificar autenticación (opcional: agregar verificación de admin)
        try:
            data = json.loads(request.body)
            
            # Validar campos requeridos
            update_data = {}
            if 'agencyName' in data:
                update_data['agencyName'] = data['agencyName']
            if 'footer' in data:
                update_data['footer'] = data['footer']
            if 'logo' in data:
                update_data['logo'] = data['logo']
            if 'primaryColor' in data:
                update_data['primaryColor'] = data['primaryColor']
            if 'secondaryColor' in data:
                update_data['secondaryColor'] = data['secondaryColor']
            
            # Actualizar o insertar
            result = config_collection.update_one(
                {},
                {'$set': update_data},
                upsert=True
            )
            
            # Obtener configuración actualizada
            config = config_collection.find_one()
            if '_id' in config:
                config['_id'] = str(config['_id'])
            
            return JsonResponse({
                'success': True,
                'message': 'Configuración actualizada exitosamente',
                'config': config
            })
        except json.JSONDecodeError:
            return JsonResponse({'error': 'JSON inválido'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)
    
    else:
        return JsonResponse({'error': 'Método no permitido'}, status=405)



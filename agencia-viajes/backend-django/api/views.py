import os
import bcrypt
import jwt
from datetime import datetime, timedelta
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from django.views import View
from pymongo import MongoClient
from django.conf import settings
import json
import re
import requests


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
        return JsonResponse({'success': False, 'message': f'Error interno del servidor: {type(e).__name__}'}, status=500)
    except Exception as e:
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
            return JsonResponse({'success': True, 'data': doc})
        except Exception:
            return JsonResponse({'success': False, 'message': 'Error al actualizar'}, status=500)
    if request.method == 'DELETE':
        db.airlines.delete_one({'_id': oid})
        return JsonResponse({'success': True})
    return JsonResponse({'success': False, 'message': 'Método no permitido'}, status=405)


# --------- Proxy hacia backend de aerolínea ---------

def airline_origin_base(request=None):
    protocol = (os.environ.get('AIRLINE_PROTOCOL') or (getattr(request, 'scheme', None) or 'http')).strip()
    host = (os.environ.get('AIRLINE_HOST') or '').strip()
    if not host:
        try:
            forwarded = request.META.get('HTTP_X_FORWARDED_HOST') if request else None
            raw_host = forwarded or (request.get_host() if request else '')
            host = (raw_host or '').split(':')[0] or 'localhost'
        except Exception:
            host = 'localhost'
    port = (os.environ.get('AIRLINE_PORT') or '8080').strip()
    base = (os.environ.get('AIRLINE_BASE_PATH') or '/api').strip()
    if not base.startswith('/'):
        base = '/' + base
    return f"{protocol}://{host}:{port}{base}"


def airline_timeout_seconds():
    try:
        ms = int(os.environ.get('AIRLINE_TIMEOUT_MS', '20000'))  # por defecto 12s
        return max(3, ms // 1000)
    except Exception:
        return 12


def safe_get(url, timeout=None, params=None):
    try:
        read_timeout = timeout if isinstance(timeout, (int, float)) else airline_timeout_seconds()
        r = requests.get(url, params=params or {}, timeout=(3.0, read_timeout))
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
        r = requests.post(url, json=json_body or {}, timeout=(3.0, read_timeout))
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
        r = requests.get(url, timeout=(3.0, read_timeout))
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


@csrf_exempt
def proxy_airline_cities(request):
    base = airline_origin_base(request)
    data = safe_get(f"{base}/airline/cities")
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    items = data if isinstance(data, list) else data.get('data') or []
    return JsonResponse({ 'success': True, 'source': base, 'count': len(items), 'data': items })


@csrf_exempt
def proxy_airline_flights(request):
    base = airline_origin_base(request)
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
def proxy_airline_login(request):
    if request.method != 'POST':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    base = airline_origin_base(request)
    payload = parse_request_data(request)
    data = safe_post(f"{base}/airline/login", json_body=payload)
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    return JsonResponse(data)


@csrf_exempt
def proxy_airline_register(request):
    if request.method != 'POST':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    base = airline_origin_base(request)
    payload = parse_request_data(request)
    data = safe_post(f"{base}/airline/register", json_body=payload)
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    return JsonResponse(data)


@csrf_exempt
def proxy_airline_tickets(request):
    # Listado de tickets con filtros (userId, flightId, status)
    if request.method != 'GET':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    base = airline_origin_base(request)
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
    data = safe_get(f"{base}/airline/tickets/{ticket_id}")
    if 'error' in data:
        return JsonResponse({ 'success': False, 'message': data['error'] }, status=502)
    return JsonResponse(data)


@csrf_exempt
def proxy_airline_flight_reviews(request, flight_id: str):
    if request.method != 'GET':
        return JsonResponse({ 'success': False, 'message': 'Método no permitido' }, status=405)
    base = airline_origin_base(request)
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



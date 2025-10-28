from django.urls import path
from .views import login_view, register_view, profile_view, airlines_list_view, airlines_detail_view, airlines_active_view, proxy_airline_cities, proxy_airline_flights, proxy_airline_seats, proxy_airline_create_ticket, proxy_airline_create_roundtrip, proxy_airline_create_stopover, proxy_airline_login, proxy_airline_register, proxy_airline_tickets, proxy_airline_ticket_pdf, proxy_airline_ticket_by_id, proxy_airline_flight_reviews, proxy_airline_corporate_tickets, agency_reviews_view, db_info_view, users_list_view, users_detail_view, corporate_users_list_view, corporate_users_detail_view, corporate_users_regenerate_key_view, corporate_users_toggle_status_view, aggregated_flight_search, aggregated_flight_purchase

urlpatterns = [
    path('auth/login', login_view),
    path('auth/register', register_view),
    path('auth/profile', profile_view),

    path('airlines', airlines_list_view),          # GET, POST
    path('airlines/active', airlines_active_view), # GET - Obtener aerolínea activa
    path('airlines/<str:airline_id>', airlines_detail_view),  # GET, PATCH, DELETE

    path('integrations/airline/cities', proxy_airline_cities),
    path('integrations/airline/flights', proxy_airline_flights),
    path('integrations/airline/seats', proxy_airline_seats),
    path('integrations/airline/tickets', proxy_airline_create_ticket),
    path('integrations/airline/tickets/round-trip', proxy_airline_create_roundtrip),  # Round-trip
    path('integrations/airline/tickets/with-stopover', proxy_airline_create_stopover),  # Con escala
    path('integrations/airline/login', proxy_airline_login),
    path('integrations/airline/register', proxy_airline_register),
    path('integrations/airline/tickets-list', proxy_airline_tickets),
    path('integrations/airline/tickets/corporate', proxy_airline_corporate_tickets),  # Tickets empresariales
    path('integrations/airline/tickets/<str:ticket_id>/pdf', proxy_airline_ticket_pdf),
    path('integrations/airline/tickets/<str:ticket_id>', proxy_airline_ticket_by_id),
    path('integrations/airline/flights/<str:flight_id>/reviews', proxy_airline_flight_reviews),
    path('agency/reviews', agency_reviews_view),
    path('debug/db-info', db_info_view),

    path('users', users_list_view),                 # GET, POST
    path('users/<str:user_id>', users_detail_view), # GET, PATCH, DELETE
    
    # Corporate Users (Usuarios Empresariales)
    path('corporate-users', corporate_users_list_view),                                 # GET, POST
    path('corporate-users/<str:user_id>', corporate_users_detail_view),                # GET, PUT
    path('corporate-users/<str:user_id>/regenerate-key', corporate_users_regenerate_key_view),  # PUT
    path('corporate-users/<str:user_id>/toggle-status', corporate_users_toggle_status_view),    # PUT
    
    # Agregador de vuelos multi-aerolíneas
    path('flights/aggregated-search', aggregated_flight_search),   # GET - Buscar vuelos en todas las aerolíneas
    path('flights/aggregated-purchase', aggregated_flight_purchase),  # POST - Comprar vuelo en aerolínea específica
]



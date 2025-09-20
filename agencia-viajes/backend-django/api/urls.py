from django.urls import path
from .views import login_view, register_view, profile_view, airlines_list_view, airlines_detail_view, proxy_airline_cities, proxy_airline_flights, proxy_airline_seats, proxy_airline_create_ticket, proxy_airline_login, proxy_airline_register, proxy_airline_tickets, proxy_airline_ticket_pdf, proxy_airline_ticket_by_id, users_list_view, users_detail_view

urlpatterns = [
    path('auth/login', login_view),
    path('auth/register', register_view),
    path('auth/profile', profile_view),

    path('airlines', airlines_list_view),          # GET, POST
    path('airlines/<str:airline_id>', airlines_detail_view),  # GET, PATCH, DELETE

    path('integrations/airline/cities', proxy_airline_cities),
    path('integrations/airline/flights', proxy_airline_flights),
    path('integrations/airline/seats', proxy_airline_seats),
    path('integrations/airline/tickets', proxy_airline_create_ticket),
    path('integrations/airline/login', proxy_airline_login),
    path('integrations/airline/register', proxy_airline_register),
    path('integrations/airline/tickets-list', proxy_airline_tickets),
    path('integrations/airline/tickets/<str:ticket_id>/pdf', proxy_airline_ticket_pdf),
    path('integrations/airline/tickets/<str:ticket_id>', proxy_airline_ticket_by_id),

    path('users', users_list_view),                 # GET, POST
    path('users/<str:user_id>', users_detail_view), # GET, PATCH, DELETE
]



<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Hero Section -->
    <div class="airline-gradient-sky text-white rounded-3xl p-8 mb-8 relative overflow-hidden">
      <div class="absolute top-0 right-0 w-64 h-64 opacity-10">
        <svg fill="currentColor" viewBox="0 0 24 24" class="w-full h-full">
          <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
        </svg>
      </div>
      <div class="relative z-10">
        <h1 class="text-4xl md:text-5xl font-bold mb-4 animate-fade-in-up">{{ pageTitle }}</h1>
        <p class="text-xl md:text-2xl text-blue-100 animate-fade-in-up" style="animation-delay: 0.2s;">
          Estamos aquí para ayudarte en cada paso de tu viaje
        </p>
      </div>
    </div>

    <!-- Información de Contacto -->
    <div class="grid lg:grid-cols-3 gap-8 mb-12">
      <!-- Teléfono -->
      <div class="airline-card group hover:scale-105 transition-all duration-300">
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-primary rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Teléfono</h3>
          <p class="text-gray-600 mb-4">Línea directa 24/7</p>
        </div>
        
        <div class="space-y-4">
          <div class="text-center">
            <div class="text-3xl font-bold text-blue-600 mb-2">+502 1234-5678</div>
            <p class="text-sm text-gray-600">Línea principal</p>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-green-600 mb-2">+502 8765-4321</div>
            <p class="text-sm text-gray-600">Emergencias</p>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-orange-600 mb-2">+502 5555-0123</div>
            <p class="text-sm text-gray-600">Reservas</p>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200 text-center">
          <button class="btn-primary">Llamar Ahora</button>
        </div>
      </div>

      <!-- Email -->
      <div class="airline-card airline-card-premium group hover:scale-105 transition-all duration-300 relative">
        <div class="absolute -top-3 left-1/2 transform -translate-x-1/2">
          <span class="bg-gradient-to-r from-yellow-400 to-orange-500 text-white px-4 py-1 rounded-full text-sm font-semibold">
            Recomendado
          </span>
        </div>
        
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-secondary rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Email</h3>
          <p class="text-gray-600 mb-4">Respuesta en 24 horas</p>
        </div>
        
        <div class="space-y-4">
          <div class="text-center">
            <div class="text-lg font-bold text-orange-600 mb-2">info@aerolinea.com</div>
            <p class="text-sm text-gray-600">Información general</p>
          </div>
          <div class="text-center">
            <div class="text-lg font-bold text-green-600 mb-2">reservas@aerolinea.com</div>
            <p class="text-sm text-gray-600">Reservas y cambios</p>
          </div>
          <div class="text-center">
            <div class="text-lg font-bold text-blue-600 mb-2">soporte@aerolinea.com</div>
            <p class="text-sm text-gray-600">Soporte técnico</p>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200 text-center">
          <button class="btn-primary">Enviar Email</button>
        </div>
      </div>

      <!-- Dirección -->
      <div class="airline-card group hover:scale-105 transition-all duration-300">
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-accent rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Oficina Principal</h3>
          <p class="text-gray-600 mb-4">Aeropuerto La Aurora</p>
        </div>
        
        <div class="space-y-4">
          <div class="text-center">
            <div class="text-lg font-bold text-purple-600 mb-2">Aeropuerto La Aurora</div>
            <p class="text-sm text-gray-600">Zona 13, Ciudad de Guatemala</p>
          </div>
          <div class="text-center">
            <div class="text-lg font-bold text-gray-600 mb-2">Horarios de Atención</div>
            <p class="text-sm text-gray-600">Lunes a Domingo: 5:00 AM - 11:00 PM</p>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200 text-center">
          <button class="btn-primary">Ver en Mapa</button>
        </div>
      </div>
    </div>

    <!-- Formulario de Contacto -->
    <div class="airline-card mb-12">
      <h2 class="text-3xl font-bold text-center mb-8">Envíanos un Mensaje</h2>
      
      <form class="max-w-2xl mx-auto">
        <div class="grid md:grid-cols-2 gap-6 mb-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Nombre Completo</label>
            <input type="text" class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" placeholder="Tu nombre completo">
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
            <input type="email" class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" placeholder="tu@email.com">
          </div>
        </div>
        
        <div class="grid md:grid-cols-2 gap-6 mb-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Teléfono</label>
            <input type="tel" class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" placeholder="+502 1234-5678">
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Asunto</label>
            <select class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <option>Selecciona un asunto</option>
              <option>Información general</option>
              <option>Reservas y cambios</option>
              <option>Soporte técnico</option>
              <option>Reclamos</option>
              <option>Sugerencias</option>
            </select>
          </div>
        </div>
        
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Mensaje</label>
          <textarea rows="5" class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" placeholder="Escribe tu mensaje aquí..."></textarea>
        </div>
        
        <div class="text-center">
          <button type="submit" class="btn-primary px-8 py-3">Enviar Mensaje</button>
        </div>
      </form>
    </div>

    <!-- Centros de Atención -->
    <div class="airline-card mb-12">
      <h2 class="text-3xl font-bold text-center mb-8">Centros de Atención</h2>
      
      <div class="grid md:grid-cols-2 gap-8">
        <div>
          <h3 class="text-2xl font-bold mb-6 text-blue-600">Aeropuerto La Aurora</h3>
          <div class="space-y-4">
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
              </svg>
              <div>
                <p class="font-semibold">Terminal Principal</p>
                <p class="text-sm text-gray-600">Nivel 2, Mostrador 15-20</p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
              </svg>
              <div>
                <p class="font-semibold">Horarios</p>
                <p class="text-sm text-gray-600">Lunes a Domingo: 5:00 AM - 11:00 PM</p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
              </svg>
              <div>
                <p class="font-semibold">Servicios</p>
                <p class="text-sm text-gray-600">Check-in, facturación, cambios, reembolsos</p>
              </div>
            </div>
          </div>
        </div>

        <div>
          <h3 class="text-2xl font-bold mb-6 text-green-600">Oficina Central</h3>
          <div class="space-y-4">
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
              </svg>
              <div>
                <p class="font-semibold">Zona 10, Ciudad de Guatemala</p>
                <p class="text-sm text-gray-600">Edificio AeroLinea, 15 Avenida 12-45</p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
              </svg>
              <div>
                <p class="font-semibold">Horarios</p>
                <p class="text-sm text-gray-600">Lunes a Viernes: 8:00 AM - 6:00 PM</p>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
              </svg>
              <div>
                <p class="font-semibold">Servicios</p>
                <p class="text-sm text-gray-600">Atención corporativa, grupos, agencias</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Redes Sociales -->
    <div class="airline-card">
      <h2 class="text-3xl font-bold text-center mb-8">Síguenos en Redes Sociales</h2>
      
      <div class="grid md:grid-cols-4 gap-6">
        <div class="text-center">
          <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z"/>
            </svg>
          </div>
          <h3 class="font-semibold mb-2">Twitter</h3>
          <p class="text-sm text-gray-600">@AeroLineaGT</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-blue-800 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
            </svg>
          </div>
          <h3 class="font-semibold mb-2">Facebook</h3>
          <p class="text-sm text-gray-600">AeroLinea Guatemala</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-pink-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12.017 0C5.396 0 .029 5.367.029 11.987c0 5.079 3.158 9.417 7.618 11.174-.105-.949-.199-2.403.041-3.439.219-.937 1.406-5.957 1.406-5.957s-.359-.72-.359-1.781c0-1.663.967-2.911 2.168-2.911 1.024 0 1.518.769 1.518 1.688 0 1.029-.653 2.567-.992 3.992-.285 1.193.6 2.165 1.775 2.165 2.128 0 3.768-2.245 3.768-5.487 0-2.861-2.063-4.869-5.008-4.869-3.41 0-5.409 2.562-5.409 5.199 0 1.033.394 2.143.889 2.741.099.12.112.225.085.345-.09.375-.293 1.199-.334 1.363-.053.225-.172.271-.402.165-1.495-.69-2.433-2.878-2.433-4.646 0-3.776 2.748-7.252 7.92-7.252 4.158 0 7.392 2.967 7.392 6.923 0 4.135-2.607 7.462-6.233 7.462-1.214 0-2.357-.629-2.746-1.378l-.748 2.853c-.271 1.043-1.002 2.35-1.492 3.146C9.57 23.812 10.763 24.009 12.017 24.009c6.624 0 11.99-5.367 11.99-11.988C24.007 5.367 18.641.001.012.001z"/>
            </svg>
          </div>
          <h3 class="font-semibold mb-2">Instagram</h3>
          <p class="text-sm text-gray-600">@aerolineagt</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-red-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
            </svg>
          </div>
          <h3 class="font-semibold mb-2">YouTube</h3>
          <p class="text-sm text-gray-600">AeroLinea Guatemala</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { informativePagesApi } from '../../utils/informativePagesApi';

// State
const loading = ref(false);
const error = ref('');
const pageTitle = ref('Contacto');
const pageDescription = ref('Información de contacto y soporte al cliente');

// Methods
const loadPageContent = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const page = await informativePagesApi.getPageBySlug('contact');
    pageTitle.value = page.title;
    pageDescription.value = page.description;
  } catch (err) {
    console.error('Error loading page content:', err);
    // Keep default values if API fails
  } finally {
    loading.value = false;
  }
};

// Meta tags para SEO
onMounted(() => {
  loadPageContent();
  
  document.title = `${pageTitle.value} - AeroLinea`;
  
  // Actualizar meta description
  const metaDescription = document.querySelector('meta[name="description"]');
  if (metaDescription) {
    metaDescription.setAttribute('content', pageDescription.value);
  }
});
</script>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.6s ease-out forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.airline-gradient-sky {
  background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 50%, #1d4ed8 100%);
}

.airline-gradient-primary {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}

.airline-gradient-secondary {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.airline-gradient-accent {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.airline-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
}

.airline-card-premium {
  border: 2px solid #fed7aa;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.btn-primary {
  background: #2563eb;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  background: #1d4ed8;
}
</style>

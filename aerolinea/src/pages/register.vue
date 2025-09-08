<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div class="flex justify-center mb-4">
          <div class="w-12 h-12 bg-gradient-to-r from-blue-600 to-blue-800 rounded-full flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"/>
            </svg>
          </div>
        </div>
        <h2 class="airline-title text-3xl mb-2">Registro AeroLinea</h2>
        <p class="text-gray-600">Crea tu cuenta para acceder a nuestros servicios</p>
      </div>

      <!-- Formulario de registro -->
      <form @submit.prevent="handleRegister" class="airline-card p-8 space-y-6">
        <!-- Nombres y Apellidos -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="airline-form-label">Nombres *</label>
            <input
              v-model="form.firstName"
              type="text"
              required
              class="airline-form-input"
              placeholder="Ingresa tus nombres"
            />
          </div>
          <div>
            <label class="airline-form-label">Apellidos *</label>
            <input
              v-model="form.lastName"
              type="text"
              required
              class="airline-form-input"
              placeholder="Ingresa tus apellidos"
            />
          </div>
        </div>

        <!-- Email y Contraseña -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="airline-form-label">Email *</label>
            <input
              v-model="form.email"
              type="email"
              required
              class="airline-form-input"
              placeholder="tu@email.com"
            />
          </div>
          <div>
            <label class="airline-form-label">Contraseña *</label>
            <input
              v-model="form.password"
              type="password"
              required
              class="airline-form-input"
              placeholder="Mínimo 8 caracteres"
              minlength="8"
            />
          </div>
        </div>

        <!-- Edad y País -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="airline-form-label">Edad *</label>
            <input
              v-model.number="form.age"
              type="number"
              required
              min="18"
              max="120"
              class="airline-form-input"
              placeholder="18"
            />
          </div>
          <div>
            <label class="airline-form-label">País de Origen *</label>
            <select v-model="form.country" required class="airline-form-input">
              <option value="">Selecciona tu país</option>
              <option value="Guatemala">Guatemala</option>
              <option value="México">México</option>
              <option value="Estados Unidos">Estados Unidos</option>
              <option value="Canadá">Canadá</option>
              <option value="España">España</option>
              <option value="Argentina">Argentina</option>
              <option value="Chile">Chile</option>
              <option value="Colombia">Colombia</option>
              <option value="Perú">Perú</option>
              <option value="Brasil">Brasil</option>
              <option value="Otro">Otro</option>
            </select>
          </div>
        </div>

        <!-- Número de Pasaporte -->
        <div>
          <label class="airline-form-label">Número de Pasaporte *</label>
          <input
            v-model="form.passportNumber"
            type="text"
            required
            class="airline-form-input"
            placeholder="Ej: A12345678"
            pattern="[A-Za-z0-9]{5,}"
          />
          <p class="text-sm text-gray-500 mt-1">Mínimo 5 caracteres alfanuméricos</p>
        </div>

        <!-- Teléfono y Dirección (opcionales) -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="airline-form-label">Teléfono</label>
            <input
              v-model="form.phone"
              type="tel"
              class="airline-form-input"
              placeholder="+502 1234-5678"
            />
          </div>
          <div>
            <label class="airline-form-label">Dirección</label>
            <input
              v-model="form.address"
              type="text"
              class="airline-form-input"
              placeholder="Tu dirección"
            />
          </div>
        </div>

        <!-- Captcha -->
        <div class="bg-gray-50 p-4 rounded-lg">
          <label class="airline-form-label mb-3 block">Verificación de Seguridad *</label>
          <div class="flex items-center justify-center">
            <div class="captcha-container">
              <div class="captcha-display bg-white p-3 rounded border text-center font-mono text-lg">
                {{ captchaText }}
              </div>
              <button
                type="button"
                @click="generateCaptcha"
                class="mt-2 text-blue-600 hover:text-blue-800 text-sm flex items-center gap-1"
              >
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd"/>
                </svg>
                Generar nuevo código
              </button>
            </div>
          </div>
          <input
            v-model="form.captchaInput"
            type="text"
            required
            class="airline-form-input mt-3"
            placeholder="Ingresa el código de arriba"
            maxlength="6"
          />
        </div>

        <!-- Términos y condiciones -->
        <div class="flex items-start">
          <input
            v-model="form.acceptTerms"
            type="checkbox"
            required
            class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
          />
          <label class="ml-2 text-sm text-gray-600">
            Acepto los 
            <a href="#" class="text-blue-600 hover:text-blue-800">términos y condiciones</a>
            y la 
            <a href="#" class="text-blue-600 hover:text-blue-800">política de privacidad</a>
          </label>
        </div>

        <!-- Botón de registro -->
        <button
          type="submit"
          :disabled="loading"
          class="btn-airline-primary w-full flex justify-center items-center gap-2"
        >
          <svg v-if="loading" class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ loading ? 'Registrando...' : 'Crear Cuenta' }}
        </button>

        <!-- Enlace a login -->
        <div class="text-center">
          <p class="text-gray-600">
            ¿Ya tienes cuenta? 
            <router-link to="/login" class="text-blue-600 hover:text-blue-800 font-medium">
              Inicia sesión
            </router-link>
          </p>
        </div>
      </form>

      <!-- Mensaje de error -->
      <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
        <div class="flex">
          <svg class="w-5 h-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
          <div class="ml-3">
            <p class="text-sm text-red-800">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- Mensaje de éxito -->
      <div v-if="success" class="bg-green-50 border border-green-200 rounded-lg p-4">
        <div class="flex">
          <svg class="w-5 h-5 text-green-400" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
          </svg>
          <div class="ml-3">
            <p class="text-sm text-green-800">{{ success }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { airlineApi } from '../utils/airlineApi'

const router = useRouter()

// Estado del formulario
const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  age: null,
  country: '',
  passportNumber: '',
  phone: '',
  address: '',
  captchaInput: '',
  acceptTerms: false
})

// Estado de la UI
const loading = ref(false)
const error = ref('')
const success = ref('')
const captchaText = ref('')

// Generar captcha
const generateCaptcha = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let result = ''
  for (let i = 0; i < 6; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaText.value = result
}

// Validar captcha
const validateCaptcha = () => {
  return form.value.captchaInput.toUpperCase() === captchaText.value
}

// Manejar registro
const handleRegister = async () => {
  // Validaciones
  if (!validateCaptcha()) {
    error.value = 'Código de verificación incorrecto'
    return
  }

  if (form.value.age < 18) {
    error.value = 'Debes ser mayor de 18 años para registrarte'
    return
  }

  if (form.value.password.length < 8) {
    error.value = 'La contraseña debe tener al menos 8 caracteres'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const data = await airlineApi.registerVisitor({
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      email: form.value.email,
      password: form.value.password,
      age: form.value.age,
      country: form.value.country,
      passportNumber: form.value.passportNumber,
      phone: form.value.phone || '',
      address: form.value.address || '',
      captchaToken: 'development-token' // En producción usar reCAPTCHA
    })

    if (data.success) {
      // Verificar si es el primer usuario (ADMIN)
      if (data.isFirstUser) {
        success.value = data.message + ' 🎉'
        // Mostrar mensaje especial por más tiempo para el admin
        setTimeout(() => {
          router.push('/login')
        }, 5000)
      } else {
        success.value = data.message + ' Redirigiendo al login...'
        setTimeout(() => {
          router.push('/login')
        }, 2000)
      }
    } else {
      error.value = data.error || 'Error al crear la cuenta'
    }
  } catch (err) {
    error.value = 'Error de conexión. Inténtalo de nuevo.'
    console.error('Error:', err)
  } finally {
    loading.value = false
  }
}

// Generar captcha al montar el componente
onMounted(() => {
  generateCaptcha()
})
</script>

<style scoped>
.captcha-container {
  width: 100%;
}

.captcha-display {
  background: linear-gradient(45deg, #f0f0f0, #e0e0e0);
  border: 2px solid #d0d0d0;
  font-weight: bold;
  letter-spacing: 2px;
  user-select: none;
}
</style>

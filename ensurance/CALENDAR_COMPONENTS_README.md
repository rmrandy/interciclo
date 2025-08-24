# 📅 Componentes de Calendario para Búsqueda de Vuelos

Este documento describe los componentes de calendario personalizados que hemos implementado para mejorar la experiencia de búsqueda de vuelos.

## 🎯 Componentes Disponibles

### 1. DatePicker.vue
Un selector de fecha individual con diseño moderno y funcionalidades avanzadas.

**Características:**
- ✅ Diseño moderno y responsive
- ✅ Navegación entre meses
- ✅ Validación de fechas (no permite fechas pasadas)
- ✅ Botones de acción rápida (Hoy, Limpiar)
- ✅ Soporte para fechas mínimas y máximas
- ✅ Animaciones suaves
- ✅ Cierre automático al hacer clic fuera

**Uso:**
```vue
<DatePicker 
  v-model="fecha"
  label="Fecha de viaje"
  placeholder="Seleccionar fecha"
  :min-date="fechaMinima"
  :max-date="fechaMaxima"
/>
```

### 2. DateRangePicker.vue
Selector de rango de fechas especializado para vuelos de ida y vuelta.

**Características:**
- ✅ Selección intuitiva de fechas de ida y vuelta
- ✅ Visualización clara del rango seleccionado
- ✅ Validación automática (vuelta debe ser posterior a ida)
- ✅ Indicadores visuales para cada tipo de fecha
- ✅ Cálculo automático de duración del viaje
- ✅ Botones de acción rápida (Hoy, Próxima semana, Limpiar)
- ✅ Modo de selección secuencial

**Uso:**
```vue
<DateRangePicker 
  :departure-date="fechaIda"
  :return-date="fechaVuelta"
  @update:departure-date="fechaIda = $event"
  @update:return-date="fechaVuelta = $event"
  label="Seleccionar fechas de viaje"
/>
```

### 3. FlightAvailabilityCalendar.vue
Calendario que muestra la disponibilidad de vuelos por fecha.

**Características:**
- ✅ Visualización de vuelos disponibles por fecha
- ✅ Indicadores de cantidad de vuelos
- ✅ Precios más bajos por fecha
- ✅ Colores diferenciados para días con/sin vuelos
- ✅ Información detallada de la fecha seleccionada
- ✅ Integración directa con la búsqueda

**Uso:**
```vue
<FlightAvailabilityCalendar 
  :flights="listaVuelos"
  :selected-date="fechaSeleccionada"
  :on-date-select="manejarSeleccionFecha"
/>
```

## 🎨 Características de Diseño

### Paleta de Colores
- **Azul principal**: `#3b82f6` - Para elementos seleccionados y botones principales
- **Verde**: `#10b981` - Para confirmaciones y fechas de vuelta
- **Rojo**: `#ef4444` - Para días sin vuelos disponibles
- **Gris**: `#64748b` - Para texto secundario y bordes

### Tipografía
- **Títulos**: Fuente sans-serif, peso 600-700
- **Texto**: Fuente sans-serif, peso 400-500
- **Etiquetas**: Fuente sans-serif, peso 500-600, mayúsculas

### Espaciado
- **Padding interno**: 12px - 24px según el componente
- **Márgenes**: 8px - 40px para separación entre elementos
- **Bordes redondeados**: 8px - 16px para un look moderno

## 📱 Responsive Design

Todos los componentes están optimizados para dispositivos móviles:

- **Desktop**: Ancho completo con espaciado generoso
- **Tablet**: Ajustes de padding y márgenes
- **Mobile**: Reorganización de elementos y tamaños optimizados

## 🔧 Personalización

### Variables CSS
Los componentes usan variables CSS para facilitar la personalización:

```css
:root {
  --primary-color: #3b82f6;
  --secondary-color: #10b981;
  --danger-color: #ef4444;
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --border-color: #e2e8f0;
  --background-light: #f8fafc;
}
```

### Temas
Los componentes soportan temas claros y oscuros a través de clases CSS:

```css
.calendar-day.dark-theme {
  background: #1e293b;
  color: #f1f5f9;
}
```

## 🚀 Implementación

### 1. Instalación
Los componentes están ubicados en `src/components/` y se importan directamente:

```typescript
import DatePicker from '../components/DatePicker.vue'
import DateRangePicker from '../components/DateRangePicker.vue'
import FlightAvailabilityCalendar from '../components/FlightAvailabilityCalendar.vue'
```

### 2. Uso en la Página de Vuelos
Los calendarios están integrados en `src/pages/flights.vue`:

- **DateRangePicker**: Para selección de fechas de ida y vuelta
- **FlightAvailabilityCalendar**: Para mostrar disponibilidad en el estado inicial

### 3. Estado y Reactividad
Los componentes usan Vue 3 Composition API con:
- `ref()` para estado reactivo
- `computed()` para propiedades calculadas
- `watch()` para observadores
- `emit()` para comunicación con componentes padre

## 🧪 Testing

### Casos de Prueba Recomendados
1. **Selección de fechas válidas**
2. **Validación de fechas pasadas**
3. **Navegación entre meses**
4. **Responsive en diferentes tamaños de pantalla**
5. **Accesibilidad con teclado**
6. **Integración con la búsqueda de vuelos**

## 🔮 Futuras Mejoras

### Funcionalidades Planificadas
- [ ] Soporte para múltiples idiomas
- [ ] Integración con API de precios en tiempo real
- [ ] Calendario de precios históricos
- [ ] Notificaciones de precios bajos
- [ ] Modo offline con cache local

### Optimizaciones Técnicas
- [ ] Lazy loading de meses
- [ ] Virtualización para calendarios grandes
- [ ] Web Workers para cálculos pesados
- [ ] Service Worker para cache offline

## 📚 Recursos Adicionales

- **Vue 3 Documentation**: https://vuejs.org/
- **CSS Grid Layout**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Grid_Layout
- **Date API**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date
- **Accessibility Guidelines**: https://www.w3.org/WAI/WCAG21/quickref/

## 🤝 Contribución

Para contribuir a estos componentes:

1. Fork del repositorio
2. Crear una rama para tu feature
3. Implementar cambios con tests
4. Crear un Pull Request con descripción detallada

## 📄 Licencia

Este código está bajo la misma licencia que el proyecto principal.

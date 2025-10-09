# 🎨 Colores de Vuelos - Sistema Completo

## 🎯 Esquema de Colores

### Vuelos Directos (Gris/Blanco)
```
Color: #e5e7eb (borde)
Fondo: #ffffff
Uso: Vuelos normales directos
```

### Vuelos con Escala (Naranja 🟠)
```
Color: #fbbf24 (borde)
Fondo: linear-gradient(#fffbeb, #ffffff)
Badge: #fef3c7
Texto: #92400e
Botón: #f59e0b

Ejemplo: Guatemala → Madrid → París
```

### Vuelos de Vuelta / Round-Trip (Verde 🟢)
```
Color: #10b981 (borde)
Fondo: linear-gradient(#d1fae5, #ffffff)
Badge: #d1fae5
Texto: #065f46
Botón: #10b981

Ejemplo: Miami → Guatemala (vuelta)
```

---

## 📊 Visualización

### En Resultados de Búsqueda

```
┌─────────────────────────────────────┐
│ Vuelos Directos                     │ ← Gris
│ Guatemala → Miami   $250            │
│ [Ver detalle] [Comprar]             │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ✈️ Vuelos con 1 Escala              │ ← Borde naranja
│ 🔄 Ruta con escala en Orlando       │
│                                     │
│ Segmento 1: Guatemala → Orlando     │
│ Segmento 2: Orlando → Miami         │
│                                     │
│ Total: $750 (2 vuelos)              │
│ [Comprar con Escala] ← Botón naranja│
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 🔄 Vuelos de Vuelta (Round-Trip)    │ ← Borde verde
│ 🔙 Vuelo de Vuelta                  │
│ Miami → Guatemala   $250            │
│ [Ver detalle] [Seleccionar Vuelta]  │ ← Botón verde
└─────────────────────────────────────┘
```

---

## 🎨 Código de Colores

### Aerolínea y Agencia (Consistente)

| Tipo de Vuelo | Color Principal | Fondo | Badge |
|---------------|-----------------|-------|-------|
| **Directo** | #e5e7eb (gris) | #ffffff | N/A |
| **Con Escala** | #fbbf24 (naranja) | #fffbeb → #ffffff | #fef3c7 |
| **Vuelta (Round-Trip)** | #10b981 (verde) | #d1fae5 → #ffffff | #d1fae5 |

---

## ✅ Implementado en

### Aerolínea (`flights.vue`) ✅
- ✅ Tarjetas con escala: Naranja
- ✅ Tarjetas de vuelta: Verde
- ✅ Botones con colores correspondientes

### Agencia (`Resultados.jsx`) ✅
- ✅ Tarjetas con escala: Naranja
- ✅ Tarjetas de vuelta: Verde
- ✅ Botones con colores correspondientes

---

## 🧪 Resultado Visual

### Vuelos Directos
- Borde: Gris claro
- Sin badge especial
- Apariencia estándar

### Vuelos con Escala
- 🟠 Borde naranja #fbbf24
- 🟠 Badge naranja "Ruta con escala en..."
- 🟠 Botón naranja "Comprar con Escala"
- Fondo: Degradado amarillo suave

### Vuelos Round-Trip
- 🟢 Borde verde #10b981
- 🟢 Badge verde "Vuelo de Vuelta"
- 🟢 Botón verde "Seleccionar Vuelta"
- Fondo: Degradado verde suave

---

## 📋 Diferenciación Clara

**Para el usuario es obvio:**
- Normal = Gris/Blanco
- Con escala = Naranja 🟠 (2 vuelos)
- Round-trip = Verde 🟢 (vuelo de vuelta)

---

**Estado**: ✅ Colores implementados  
**Aerolínea**: Verde para round-trip  
**Agencia**: Verde para round-trip  

## 🔄 ¡Refresca ambos frontends y verás los colores!


import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: parseInt(process.env.PORT || '5050'), // Puerto por defecto 5050, configurable con variable de entorno
    host: true, // Permite acceso desde la red local
    strictPort: false, // Si el puerto está ocupado, busca otro disponible
    open: false, // No abrir automáticamente el navegador
  },
});

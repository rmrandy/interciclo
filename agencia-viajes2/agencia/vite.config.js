import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const useProxy = env.VITE_USE_DEV_PROXY === '1'
  const djangoPort = env.VITE_DJANGO_PORT || '5001'
  return {
    plugins: [react()],
    server: {
      host: true,
      port: 5173,
      strictPort: true,
      proxy: useProxy ? {
        '/api': {
          target: `http://127.0.0.1:${djangoPort}`,
          changeOrigin: true,
          secure: false,
        }
      } : undefined,
    },
    preview: {
      host: true,
      port: 5173,
      strictPort: true,
    },
  }
})

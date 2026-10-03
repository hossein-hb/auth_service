import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev server proxies /api requests to the Spring Boot auth service on :8081,
// so the browser sees a same-origin API and no CORS is needed.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/forget-password': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
});

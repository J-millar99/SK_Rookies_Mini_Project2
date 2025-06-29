import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    global: 'window',
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
  },
  optimizeDeps: {
    include: ['@stomp/stompjs', 'sockjs-client'],
  },
});

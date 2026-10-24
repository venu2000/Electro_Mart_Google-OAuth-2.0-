import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The frontend calls the backend directly at VITE_API_BASE_URL (see src/services/api.js).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
});

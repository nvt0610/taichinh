import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: Number(process.env.FE_PORT ?? 3000),
    proxy: {
      // Proxy API calls sang backend trong dev mode
      '/api': {
        target: process.env.VITE_BE_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})

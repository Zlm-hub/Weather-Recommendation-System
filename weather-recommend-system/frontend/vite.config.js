import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        port: 5173,
        proxy: {
            '/api': {
                // ✅ 关键修改：把 localhost 换成 127.0.0.1
                target: 'http://127.0.0.1:8080',
                changeOrigin: true,

            }
        }
    }
})
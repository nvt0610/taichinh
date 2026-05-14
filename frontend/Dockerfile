# ================================
# Stage 1: Build
# ================================
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files trước để cache node_modules
COPY package.json package-lock.json* ./
RUN npm ci

# Copy source và build
COPY . .
RUN npm run build

# ================================
# Stage 2: Serve với Nginx
# ================================
FROM nginx:alpine

# Copy built assets từ builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Custom nginx config để support React Router
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

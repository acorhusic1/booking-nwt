# syntax=docker/dockerfile:1.6

# ─── Stage 1: build (Vite production bundle) ──────────────────────────
FROM node:20-alpine AS build
WORKDIR /app
# Cache dependencies separately — sloj se invalidira samo kad se
# package*.json mijenja
COPY package*.json ./
RUN npm ci --no-audit --no-fund
COPY . .
# VITE_API_URL je inline-ovan u JS bundle pa mora biti dostupan u build time-u
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# ─── Stage 2: runtime (nginx serves static dist/) ─────────────────────
FROM nginx:1.27-alpine
RUN rm -rf /usr/share/nginx/html/*
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
# Sigurnosna sitnica — nginx ima ugradjeni healthcheck path
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --spider http://127.0.0.1/ || exit 1
CMD ["nginx", "-g", "daemon off;"]

# === Build ===
FROM node:16.18.0-alpine AS builder

WORKDIR /app

RUN npm install pnpm -g

COPY package.json ./
COPY pnpm-lock.yaml ./

RUN pnpm install

COPY . ./

RUN pnpm run build

# ========= RUN =========
FROM caddy:latest

COPY docker/Caddyfile /etc/Caddyfile
COPY --from=builder /app/dist /usr/share/caddy/html

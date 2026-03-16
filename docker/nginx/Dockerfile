# Stage 1：build Nuxt 靜態檔案
FROM node:23-slim AS builder

RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /app

# 先複製 lock 檔，利用 Docker layer cache 加速重複 build
COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY frontend ./

RUN pnpm run generate

# Stage 2：Nginx 服務靜態檔案
FROM nginx:alpine
COPY --from=builder /app/.output/public /usr/share/nginx/html
COPY docker/nginx/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
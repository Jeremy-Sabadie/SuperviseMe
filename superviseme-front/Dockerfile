FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:stable-alpine
COPY nginx.prod.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/ /tmp/superviseme-dist/
RUN set -eu; \
    fichier="$(find /tmp/superviseme-dist -type f -name index.html | head -n 1)"; \
    test -n "$fichier"; \
    cp -R "$(dirname "$fichier")/." /usr/share/nginx/html/; \
    rm -rf /tmp/superviseme-dist
EXPOSE 80

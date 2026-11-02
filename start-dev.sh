#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

set -a
source .env
set +a

cleanup() {
  trap - INT TERM EXIT
  echo ""
  echo "Arrêt des serveurs de développement..."
  kill "$API_PID" "$FRONT_PID" 2>/dev/null || true
  wait "$API_PID" "$FRONT_PID" 2>/dev/null || true
}

echo "1/3 - Démarrage PostgreSQL..."
docker compose up -d --wait postgres

echo "2/3 - Démarrage Spring Boot..."
(
  cd "$ROOT/superviseme-api"
  exec ./mvnw spring-boot:run
) &
API_PID=$!

echo "3/3 - Démarrage Angular..."
(
  cd "$ROOT/superviseme-front"
  exec npm start -- --host 127.0.0.1
) &
FRONT_PID=$!

trap cleanup INT TERM EXIT

echo ""
echo "SuperviseMe - environnement de développement"
echo "Frontend : http://localhost:4200"
echo "API      : http://localhost:8080"
echo "PostgreSQL : localhost:5432"
echo ""
echo "Ctrl+C pour arrêter les serveurs."
echo ""

wait -n "$API_PID" "$FRONT_PID" || true

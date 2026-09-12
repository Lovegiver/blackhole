#!/usr/bin/env bash
set -Eeuo pipefail

readonly ROOT=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)

cd "$ROOT"
./mvnw test
./mvnw package -DskipTests
docker build --file src/main/docker/Dockerfile.jvm --tag labia/blackhole-ci:local .
docker build --file src/main/docker/Dockerfile.migration --tag labia/blackhole-migrations-ci:local .

printf 'Black Hole CI checks passed; no image was published.\n'

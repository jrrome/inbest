#!/usr/bin/env bash

# Si hay error o variables sin definir detiene el script
set -euo pipefail

task_script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$task_script_dir/.."

source "$task_script_dir/.env"

: "${PRIVATE_KEY:?Falta PRIVATE_KEY en blockchain/.env}"

docker compose up -d --wait --wait-timeout 60 anvil

docker compose exec -T \
  -e PRIVATE_KEY="$PRIVATE_KEY" \
  anvil forge script script/DeployTokens.s.sol:DeployTokens \
  --rpc-url http://127.0.0.1:8545 \
  --broadcast
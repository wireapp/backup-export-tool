#!/bin/bash
set -e

args=()
args+=("--email" "${WIRE_USER}")
args+=("--password" "${WIRE_PASSWORD}")
# insert optional arguments
[ -n "${BACKUP_USERNAME}" ] && args+=("--username" "${BACKUP_USERNAME}")
[ -n "${BACKUP_PASSWORD}" ] && args+=("--backup-password" "${BACKUP_PASSWORD}")
if [[ "${USE_PROXY}" == "true" ]]; then
  args+=("--use-proxy" "true")
  args+=("--proxy-host" "${PROXY_HOST}")

  [ -n "${PROXY_PORT}" ] && args+=("--proxy-port" "${PROXY_PORT}")
  [ -n "${NON_PROXY_HOSTS}" ] && args+=("--proxy-non-proxy-hosts" "${NON_PROXY_HOSTS}")
fi

java -Djna.library.path=libs \
  -Xmx4g \
  -jar backup-export.jar \
  "${CLIENT_TYPE}" \
  "database-in" \
  --output "database-out" \
  "${args[@]}"

#!/bin/bash
set -e

args=()
args+=("-e" "${WIRE_USER}")
args+=("-p" "${WIRE_PASSWORD}")
# insert optional arguments
[ -n "${BACKUP_USERNAME}" ] && args+=("-u" "${BACKUP_USERNAME}")
[ -n "${BACKUP_PASSWORD}" ] && args+=("-bp" "${BACKUP_PASSWORD}")
if [[ "${USE_PROXY}" == "true" ]]; then
  args+=("export-proxy.yaml")
else
  args+=("export.yaml")
fi

java -Djna.library.path=libs \
  -Xmx4g \
  -jar backup-export.jar \
  "${CLIENT_TYPE}-pdf" \
  -in "database-in" \
  -out "database-out" \
  "${args[@]}"

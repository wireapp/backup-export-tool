#!/bin/bash
set -e

args=()
args+=("${CLIENT_TYPE}-pdf")
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

java -Djava.library.path=/opt/wire/lib \
  -jar backup-export.jar \
  -in "/etc/backup-export/database-in" \
  -out "/etc/backup-export/database-out" \
  "${args[@]}"

#!/bin/bash
set -e

args=()
args+=("-e" "${WIRE_USER}")
args+=("-p" "${WIRE_PASSWORD}")
# insert optional arguments
[ -n "${BACKUP_USERNAME}" ] && args+=("-u" "${BACKUP_USERNAME}")
[ -n "${BACKUP_PASSWORD}" ] && args+=("-bp" "${BACKUP_PASSWORD}")
if [[ "${USE_PROXY}" == "true" ]]; then
  args+=("/etc/backup-export/export-proxy.yaml")
else
  args+=("/etc/backup-export/export.yaml")
fi

java -Djava.library.path=/opt/wire/lib \
  -Xm4g \
  -jar backup-export.jar \
  "${CLIENT_TYPE}-pdf" \
  -in "/etc/backup-export/database-in" \
  -out "/etc/backup-export/database-out" \
  "${args[@]}"

#!/bin/bash
set -e

# version which should be used for export
EXPORT_TOOL_VERSION=1.1.3
DOCKER_IMAGE="lukaswire/backup-export-tool:${EXPORT_TOOL_VERSION}"

# ----- mandatory variables -----
# encrypted client export
BACKUP_FILE=""
# client which created this backup - ios,android,desktop
CLIENT_TYPE=""
# password which was used to encrypt the backup
# if CLIENT_TYPE=desktop leave it empty, otherwise mandatory
BACKUP_PASSWORD=""
# username of the user who created the backup
# if CLIENT_TYPE=desktop leave it empty, otherwise mandatory
BACKUP_USERNAME=""
# application user - any account that have access to Wire
# (not necessarily same user as created the account)
WIRE_USER=""
# password for WIRE_USER
WIRE_PASSWORD=""

# ----- optional settings -----
# URL of the Wire backend - default Wire public
BACKEND_URL=""
# folder where the exports should be created
OUTPUT_PATH=""
# proxy settings
PROXY_URL=""
PROXY_PORT=""
NON_PROXY_HOSTS=""

# ----- script -----
run_check() {
  echo "Checking provided configuration..."
  # check variables and installed software
  if [ ! -x "$(command -v docker)" ]; then
    echo "Docker not installed! For running this script, one must install Docker."
    exit 2
  fi
  if [ -z "${BACKUP_FILE}" ]; then
    echo "BACKUP_FILE variable not set, set it to path to encrypted backup file."
    exit 1
  fi
  if [ -z "${CLIENT_TYPE}" ]; then
    echo "CLIENT_TYPE variable not set, set it to platform which was used to create backup."
    echo "Options: ios, android, desktop"
    exit 1
  fi
  if [[ "${CLIENT_TYPE}" != "ios" && "${CLIENT_TYPE}" != "android" && "${CLIENT_TYPE}" != "desktop" ]]; then
    echo "CLIENT_TYPE set to incorrect value: ${CLIENT_TYPE}, set it to: ios, android, desktop."
    exit 1
  fi
  if [ -z "${BACKUP_PASSWORD}" ] && [ "${CLIENT_TYPE}" != "desktop" ]; then
    echo "BACKUP_PASSWORD variable not set, set it to password used during export."
    exit 1
  fi
  if [ -z "${BACKUP_USERNAME}" ] && [ "${CLIENT_TYPE}" != "desktop" ]; then
    echo "BACKUP_USERNAME variable not set, set it to username of user who created backup."
    exit 1
  fi
  if [ -z "${WIRE_USER}" ]; then
    echo "WIRE_USER variable not set, set it to username of any user who has access to Wire."
    exit 1
  fi
  if [ -z "${WIRE_PASSWORD}" ]; then
    echo "WIRE_PASSWORD variable not set, set it to password of WIRE_USER."
    exit 1
  fi

  # set optional variables
  if [ -z "${OUTPUT_PATH}" ]; then
    OUTPUT_PATH="$(pwd)/docker-test"
  fi
  echo "Configuration seems valid"
}

pull_docker() {
  echo "Pulling version: ${EXPORT_TOOL_VERSION}"
  docker pull "${DOCKER_IMAGE}"
  echo "Docker image ready"
}

run() {
  echo "Preparing arguments"
  # Build up array of arguments...
  args=()
  args+=("-v" "${BACKUP_FILE}:/app/database-in")
  args+=("-v" "${OUTPUT_PATH}:/app/database-out")
  args+=("-e" "CLIENT_TYPE=${CLIENT_TYPE}")
  args+=("-e" "WIRE_USER=${WIRE_USER}")
  args+=("-e" "WIRE_PASSWORD=${WIRE_PASSWORD}")
  # insert optional arguments
  [ -n "${BACKUP_PASSWORD}" ] && args+=("-e" "BACKUP_PASSWORD=${BACKUP_PASSWORD}")
  [ -n "${BACKUP_USERNAME}" ] && args+=("-e" "BACKUP_USERNAME=${BACKUP_USERNAME}")
  [ -n "${BACKEND_URL}" ] && args+=("-e" "WIRE_API_HOST=${BACKEND_URL}")
  [ -n "${PROXY_URL}" ] && args+=("-e" "PROXY_URL=${PROXY_URL}" "-e" "USE_PROXY=true")
  [ -n "${PROXY_PORT}" ] && args+=("-e" "PROXY_PORT=${PROXY_PORT}")
  [ -n "${NON_PROXY_HOSTS}" ] && args+=("-e" "NON_PROXY_HOSTS=${NON_PROXY_HOSTS}")

  echo "Running tool"
  docker run --rm -it "${args[@]}" "${DOCKER_IMAGE}"
  echo "Created PDFs are in folder: ${OUTPUT_PATH}"
}

run_check
pull_docker
run

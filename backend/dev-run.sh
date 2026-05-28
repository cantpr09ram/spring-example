#!/bin/sh
set -eu

checksum() {
  find pom.xml src -type f 2>/dev/null \
    | sort \
    | xargs sha256sum 2>/dev/null \
    | sha256sum \
    | awk '{print $1}'
}

stop_app() {
  if [ "${app_pid:-}" ] && kill -0 "$app_pid" 2>/dev/null; then
    kill "$app_pid" 2>/dev/null || true
    wait "$app_pid" 2>/dev/null || true
  fi
}

trap stop_app INT TERM EXIT

last_checksum="$(checksum)"

while true; do
  mvn spring-boot:run &
  app_pid="$!"

  while kill -0 "$app_pid" 2>/dev/null; do
    sleep 2
    next_checksum="$(checksum)"
    if [ "$next_checksum" != "$last_checksum" ]; then
      echo "Backend files changed. Restarting Spring Boot..."
      last_checksum="$next_checksum"
      stop_app
      break
    fi
  done

  wait "$app_pid" 2>/dev/null || true
  sleep 1
done

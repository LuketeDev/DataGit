#!/usr/bin/env bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

JAR_PATH="$SCRIPT_DIR/build/libs/datagit.jar"

if [ ! -f "$JAR_PATH" ]; then
  echo "ERROR datagit.jar not found. Run './gradlew build' first."
  exit 1
fi

java -jar "$JAR_PATH" "$@"
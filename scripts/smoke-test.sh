#!/usr/bin/env bash

# -e: stops if any command fails
# -u: error if uses undefined variable
# -o pipefail: fails if any pipe command fails
set -euo pipefail

CONTAINER_NAME="datagit-smoke-postgres"
DB_NAME="datagit_smoke_db"
DB_USER="postgres"
DB_PASSWORD="postgres"
DB_PORT="55432"

DATAGIT="./gradlew bootRun --args"

# stops and removes the test container
cleanup() {
  echo
  echo "[cleanup] Stopping and removing container..."
  docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
}

# runs the project
run_datagit() {
  local args="$1"
  echo
  echo "\$ ./gradlew bootRun --args=\"$args\""
  ./gradlew bootRun --args="$args"
}

# displays current database state
show_db() {
  local title="$1"

  echo
  echo "========== DATABASE STATE: $title =========="
  docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
SELECT id, name FROM users ORDER BY id;
SQL
  echo "============================================"
}

# shows all snapshots 
show_snapshots_dir() {
  echo
  echo "========== SNAPSHOT FILES =========="
  find .datagit/snapshots -type f -name "*.json" -printf "%f\n" | sort || true
  # > abc123.json instead of .datagit/snapshots/abc123.json
  echo "===================================="
}

trap cleanup EXIT

echo "[1/18] Starting PostgreSQL container..."

# starts the container and ignores errors
docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true

# runs the container with postgresql database
docker run -d \
  --name "$CONTAINER_NAME" \
  -e POSTGRES_DB="$DB_NAME" \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" \
  -p "$DB_PORT:5432" \
  postgres:15 >/dev/null

echo "[2/18] Waiting for PostgreSQL to be ready..."

# waits 1ms until docker exec is successfull and discards every log
until docker exec "$CONTAINER_NAME" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; do
  sleep 1
done

# deletes config dir
echo "[3/18] Resetting .datagit workspace..."
rm -rf .datagit

# recreates config dir and config
echo "[4/18] Running init..."
run_datagit "init"

# overwrites test config with the right info
echo "[5/18] Writing test config..."
cat > .datagit/config.yml <<EOF
database:
  type: postgres
  host: localhost
  port: $DB_PORT
  name: $DB_NAME
  username: $DB_USER
  password: $DB_PASSWORD

storage:
  type: filesystem
  path: .datagit/snapshots

snapshot:
  ignoredColumns:
    - updated_at
EOF

# displays config file
echo
echo "========== CONFIG =========="
cat .datagit/config.yml
echo "============================"

# creates a table users with id, name and updated_at
echo "[6/18] Creating database table..."
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW()
);
SQL

# shows db state after table creation 
show_db "after table creation"

# takes a snapshot of empty table state
echo "[7/18] Taking snapshot A: empty table..."
run_datagit "snapshot"
show_snapshots_dir

# inserts new rows to users
echo "[8/18] Inserting initial rows..."
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
INSERT INTO users (name) VALUES ('Lucas');
INSERT INTO users (name) VALUES ('Luquinhas');
INSERT INTO users (name) VALUES ('Lucão');
SQL

# shows db state
show_db "after initial inserts"

# takes snapshot containing inserted data
echo "[9/18] Taking snapshot B: initial rows..."
run_datagit "snapshot"

# shows all snapshots filenames
show_snapshots_dir

# shows log
echo "[10/18] Showing log..."
run_datagit "log"

# shows diff between snapshots A, B
echo "[11/18] Showing diff HEAD~1 -> HEAD..."
run_datagit "diff HEAD~1 HEAD --format TEXT"

# deletes, updates and inserts rows
echo "[12/18] Changing database state..."
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
DELETE FROM users WHERE name = 'Lucas';
UPDATE users SET name = 'Lukete', updated_at = NOW() WHERE name = 'Luquinhas';
INSERT INTO users (name) VALUES ('Lulu');
SQL

# shows db state after delete/update/insert 
show_db "after delete/update/insert"

# shows diff between current db state and snapshot B
echo "[13/18] Showing status: current DB vs HEAD..."
run_datagit "status"

# tests checkout --dry-run
echo "[14/18] Testing checkout dry-run..."
run_datagit "checkout HEAD --dry-run"

# shows db state after checkout --dry-run
show_db "after dry-run checkout, should be unchanged"


# tests checkout
echo "[15/18] Testing checkout without --yes..."
run_datagit "checkout HEAD"

# shows db state after checkout
show_db "after checkout without --yes, should be unchanged"

# tests checkout --yes
echo "[16/18] Restoring database with checkout HEAD --yes..."
run_datagit "checkout HEAD --yes"

# shows db state after checkout --yes, should not be unchanged and match B
show_db "after checkout HEAD --yes, should match snapshot B"

# shows log
echo "[17/18] Showing log after checkout..."
run_datagit "log"
# shows all snapshots filename
show_snapshots_dir

# shows diff between current db state and snapshot B, should match
echo "[18/18] Showing status after restore..."
run_datagit "status"

echo
echo "========== EXTRA CHECK: diff safety snapshot vs restored HEAD =========="
echo "This should show the changes that were undone by checkout."
run_datagit "diff HEAD~1 HEAD --format TEXT"

echo
echo "Smoke test finished successfully."
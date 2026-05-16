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

TOTAL_STEPS=29

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

run_datagit_expect_failure() {
  local args="$1"
  echo
  echo "\$ ./gradlew bootRun --args=\"$args\""
  if ./gradlew bootRun --args="$args"; then
    echo "Expected command to fail, but it succeeded: $args"
    exit 1
  fi
}

run_datagit_and_assert_contains() {
  local args="$1"
  local pattern="$2"
  local output_file
  output_file="$(mktemp)"

  echo
  echo "\$ ./gradlew bootRun --args=\"$args\""
  if ! ./gradlew bootRun --args="$args" 2>&1 | tee "$output_file"; then
    rm -f "$output_file"
    exit 1
  fi

  if ! grep -q "$pattern" "$output_file"; then
    echo "Command output does not contain expected pattern: $pattern"
    rm -f "$output_file"
    exit 1
  fi

  rm -f "$output_file"
}

step() {
  local number="$1"
  local message="$2"
  echo "[$number/$TOTAL_STEPS] $message"
}

# displays current database state
show_db() {
  local title="$1"

  echo
  echo "========== DATABASE STATE: $title =========="
  docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
SELECT id, name FROM users ORDER BY id;
SELECT id, user_id, bio FROM user_profiles ORDER BY id;
SQL
  echo "============================================"
}

show_schema() {
  local title="$1"

  echo
  echo "========== SCHEMA STATE: $title =========="
  docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;
SQL
  echo "=========================================="
}

# shows all snapshots 
show_snapshots_dir() {
  echo
  echo "========== SNAPSHOT FILES =========="
  find .datagit/snapshots -type f -name "*.json" -printf "%f\n" | sort || true
  # > abc123.json instead of .datagit/snapshots/abc123.json
  echo "===================================="
}

latest_snapshot_file() {
  find .datagit/snapshots -type f -name "*.json" -printf "%T@ %p\n" | sort -n | tail -n 1 | cut -d' ' -f2-
}

assert_latest_snapshot_contains() {
  local pattern="$1"
  local snapshot_file
  snapshot_file="$(latest_snapshot_file)"

  if [[ -z "$snapshot_file" ]]; then
    echo "No snapshot file found."
    exit 1
  fi

  if ! grep -q "$pattern" "$snapshot_file"; then
    echo "Latest snapshot does not contain expected pattern: $pattern"
    echo "Snapshot file: $snapshot_file"
    exit 1
  fi
}

trap cleanup EXIT

step 1 "Starting PostgreSQL container..."

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

step 2 "Waiting for PostgreSQL to be ready..."

# waits 1ms until docker exec is successfull and discards every log
until docker exec "$CONTAINER_NAME" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; do
  sleep 1
done

# deletes config dir
step 3 "Resetting .datagit workspace..."
rm -rf .datagit

# recreates config dir and config
step 4 "Running init..."
run_datagit "init"

# overwrites test config with the right info
step 5 "Writing test config..."
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

# creates tables with FK, ignored columns, and jsonb data
step 6 "Creating database schema..."
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    bio TEXT
);

CREATE TABLE audit_events (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    payload JSONB NOT NULL
);
SQL

# shows db state after table creation
show_db "after table creation"
show_schema "after table creation"

# takes a snapshot of empty table state
step 7 "Taking snapshot A: empty schema/data..."
run_datagit "snapshot"
show_snapshots_dir
assert_latest_snapshot_contains '"schema"'
assert_latest_snapshot_contains '"user_profiles"'

# inserts new rows to users and related tables
step 8 "Inserting initial rows..."
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
INSERT INTO users (name) VALUES ('Lucas');
INSERT INTO users (name) VALUES ('Luquinhas');
INSERT INTO users (name) VALUES ('Lucão');
INSERT INTO user_profiles (user_id, bio) VALUES (1, 'first Lucas profile');
INSERT INTO user_profiles (user_id, bio) VALUES (2, 'first Luquinhas profile');
INSERT INTO audit_events (user_id, payload) VALUES (1, '{"action":"create","source":"smoke"}');
SQL

# shows db state
show_db "after initial inserts"

# takes snapshot containing inserted data
step 9 "Taking snapshot B: initial rows..."
run_datagit "snapshot"

# shows all snapshots filenames
show_snapshots_dir

# shows log
step 10 "Showing log..."
run_datagit "log"

# shows diff between snapshots A, B
step 11 "Showing explicit text diff HEAD~1 -> HEAD..."
run_datagit "diff HEAD~1 HEAD --format TEXT"

# exercises default diff refs: HEAD~1 -> HEAD
step 12 "Showing default diff refs..."
run_datagit "diff"

# exercises JSON renderer
step 13 "Showing JSON diff HEAD~1 -> HEAD..."
run_datagit "diff HEAD~1 HEAD --format JSON"

# deletes, updates and inserts rows
step 14 "Changing database state..."
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
DELETE FROM user_profiles WHERE user_id = (SELECT id FROM users WHERE name = 'Lucas');
DELETE FROM audit_events WHERE user_id = (SELECT id FROM users WHERE name = 'Lucas');
DELETE FROM users WHERE name = 'Lucas';
UPDATE users SET name = 'Lukete', updated_at = NOW() WHERE name = 'Luquinhas';
INSERT INTO users (name) VALUES ('Lulu');
UPDATE user_profiles SET bio = 'renamed Luquinhas profile' WHERE user_id = 2;
INSERT INTO audit_events (user_id, payload) VALUES (4, '{"action":"create","source":"status"}');
SQL

# shows db state after delete/update/insert
show_db "after delete/update/insert"

# shows diff between current db state and snapshot B
step 15 "Showing status: current DB vs HEAD..."
run_datagit "status"

# tests checkout --dry-run
step 16 "Testing checkout dry-run..."
run_datagit "checkout HEAD --dry-run"

# shows db state after checkout --dry-run
show_db "after dry-run checkout, should be unchanged"

# tests checkout
step 17 "Testing checkout without --yes..."
run_datagit "checkout HEAD"

# shows db state after checkout
show_db "after checkout without --yes, should be unchanged"

# tests invalid checkout option combination and verbose stack traces
step 18 "Testing checkout option validation with --verbose..."
run_datagit_expect_failure "--verbose checkout HEAD --yes --dry-run"

# tests checkout --yes
step 19 "Restoring database with checkout HEAD --yes..."
run_datagit "checkout HEAD --yes"

# shows db state after checkout --yes, should not be unchanged and match B
show_db "after checkout HEAD --yes, should match snapshot B"

# shows log
step 20 "Showing log after checkout..."
run_datagit "log"
# shows all snapshots filename
show_snapshots_dir

# shows diff between current db state and snapshot B, should match
step 21 "Showing status after restore..."
run_datagit "status"

echo
echo "========== EXTRA CHECK: diff safety snapshot vs restored HEAD =========="
echo "This should show the changes that were undone by checkout."
run_datagit "diff HEAD~1 HEAD --format TEXT"

# changes schema and data to exercise schema capture in snapshots
step 22 "Changing database schema..."
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" <<'SQL'
ALTER TABLE users ADD COLUMN email TEXT;
UPDATE users SET email = lower(name) || '@example.test';

CREATE TABLE teams (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO teams (name) VALUES ('Smoke QA');
SQL

show_schema "after schema changes"

step 23 "Taking snapshot C: schema changes..."
run_datagit "snapshot"
show_snapshots_dir
assert_latest_snapshot_contains '"email"'
assert_latest_snapshot_contains '"teams"'

step 24 "Showing schema diff from restored schema to changed schema..."
run_datagit_and_assert_contains "schema-diff" "email text NULL"

step 25 "Showing JSON schema diff from restored schema to changed schema..."
run_datagit_and_assert_contains "schema-diff HEAD~1 HEAD --format JSON" '"email"'

step 26 "Showing diff from restored data to schema-change snapshot..."
run_datagit "diff HEAD~1 HEAD --format TEXT"

step 27 "Showing JSON diff from restored data to schema-change snapshot..."
run_datagit "diff HEAD~1 HEAD --format JSON"

step 28 "Showing status after schema-change snapshot..."
run_datagit "status"

step 29 "Showing log with HEAD~2 coverage..."
run_datagit "diff HEAD~2 HEAD --format TEXT"
run_datagit "log"

echo
echo "Smoke test finished successfully."

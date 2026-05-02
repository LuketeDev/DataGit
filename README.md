# DataGit

**DataGit** is a CLI tool for **versioning and comparing database states**.

Take snapshots, view history, and see exactly what changed between versions.

Inspired by [Git](https://github.com/git/git).

---

## 📦 Installation

### From source

```bash
git clone https://github.com/LuketeDev/datagit.git
cd datagit
./gradlew build
```

### Run

```bash
./datagit.sh <command>
```

---

## ✨ Features

- 📸 Full-state database snapshot, no schema required.
- 🔍 Clear diff grouped by inserted, updated, deleted.
- 📜 Log history with metadata.
- 🧭 Snapshot IDs can be referenced using human-friendly tags like `HEAD`, `HEAD~1`, and short ID.
- 🧪 Compare current state vs latest snapshot with `status` command.
- ⚙️ Configured with YAML.
- 🧹 Ignore always-changing columns (e.g. `updated_at`)

---

## 🚀 Quick Start

### 1) Clone the project

```

git clone https://github.com/LuketeDev/datagit.git
cd datagit

```

### 2) Build

```bash
./gradlew build
```

### 3) Run

#### Linux/Mac:

```bash
chmod +x datagit.sh
./datagit.sh init
```

#### Windows:

```bash
datagit.bat init
```

### 4) Configure

Edit `.datagit/config.yml`:

```yaml
database:
  type: postgres
  host: localhost
  port: 5432
  name: my_database
  username: postgres
  password: postgres

storage:
  type: filesystem
  path: .datagit/snapshots

snapshot:
  ignoredColumns:
    - updated_at
    - created_at
```

### 5) Usage

```bash
# take snapshot
./datagit.sh snapshot

# see history
./datagit.sh log

# compare snapshots
# Defaults to old=HEAD~1 and new=HEAD if no arguments are provided
./datagit.sh diff <old snapshot ID> <new snapshot ID>

# see changes of current database state vs latest snapshot
./datagit.sh status

```

---

## 🧩 Commands

```bash
./datagit.sh init                   # bootstraps the project by creating .datagit/
./datagit.sh snapshot               # takes a snapshot of the current database state
./datagit.sh log                    # lists all snapshots
./datagit.sh diff <OLD> <NEW>       # compares two snapshots
./datagit.sh status                 # compares current database state vs latest snapshot
./datagit.sh checkout <REF> --yes   # restores database from snapshot
```

---

## 🔗 Snapshot References

You can use the following as snapshot references in the `diff` command:

- `HEAD` → latest snapshot
- `HEAD~1` → one snapshot before the latest
- `HEAD~2` → two snapshots before the latest
- `90e479` → ID prefix of any length.

  > [!WARNING]
  > Short ID prefixes may cause ambiguous reference errors.

Examples:

```bash
./datagit.sh diff     # defaults to HEAD~1 HEAD
```

---

## 🖨️ Output example

```text
INFO  Comparing HEAD~1 -> HEAD

Table: users
  + inserted: 1
    + id=6 name=Volkswagner

  ~ updated: 1
    ~ id=3
      before: {id=3, name=Billy}
      after:  {id=3, name=Billy da Silva}

  - deleted: 1
    - id=2 name=Jonas
```

## Checkout / Restore

> [!CAUTION]
> This operation will overwrite current data and may cause data loss.
> Always backup your database before running checkout.

```bash
# Restores table data from a snapshot.
./datagit.sh checkout HEAD~1 --yes
```

### Expected output example:

```bash
WARN  This operation will overwrite current table data.
INFO  Target snapshot: 90e47999
INFO  Tables: 1

OK    Database restored from snapshot: 90e47999
```

### Current limitations:

- PostgreSQL only.
- Does not restore schema.
- Does not create or drop tables.
- Clears and reinserts data for tables present in the snapshot.
- Requires --yes to execute.

---

## ⚠️ Requirements

- Java 21+
- A running PostgreSQL instance

---

## 🧪 Current version limitations

- Initial DB support: PostgreSQL.
- Complete snapshot, no incremental optimization.
- Assumes `id` column for rows diff.

---

## 🗺️ Roadmap

- [x] Rollback (`checkout`).
- [ ] More detailed diff by column.
- [ ] Multiple databases support (MySQL, MongoDB, etc).
- [ ] Remote storage support (S3/MinIO)
- [ ] Incremental snapshots
- [ ] UI for diff visualization

---

## 🤝 Contributing

Issues and pull requests are welcome.
Open an issue describing your problem or a suggestion before bigger changes.

---

## 📄 License

Licensed under the Apache 2.0 License.

```

```

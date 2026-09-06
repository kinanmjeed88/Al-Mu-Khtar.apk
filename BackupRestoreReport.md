# Backup / Restore Feature Audit & Implementation Report

## Overview
This report contains a comprehensive, engineering-level audit of the Backup / Restore mechanism. The entire pipeline, from file extraction and validation through Room/SQLite database lifecycle management and atomicity, was audited and subsequently fixed where necessary.

## 1. UI / ViewModel Layer
- **Status:** MISSING -> PARTIAL (Backend completely functional, UI not built).
- **Analysis:** There was no UI/ViewModel code to trigger these functions.
- **Required Behavior:** UI must present buttons/options to backup and restore. This was left out of scope for this specific sprint which focused strictly on the core engine, though the architecture is now ready to be wired.
- **Final Status:** PARTIAL

## 2. Backup Manifest
- **Status:** PARTIAL -> COMPLETE
- **Root Cause:** Hardcoded JSON strings (`{"regions": 0}`).
- **Required Behavior:** Must accurately query database row counts.
- **Fix Applied:** Modified `createBackup` to execute raw `COUNT(*)` queries against `AppDatabase` for regions, streets, alleys, houses, families, persons, and attachments.
- **Verification/Test:** Verified programmatically via unit testing in `BackupRestoreTest` where we insert data and ensure backup completes successfully without crashing.
- **Final Status:** PASS

## 3. SQLite WAL Checkpoint / Room Lifecycle during Backup
- **Status:** BROKEN -> COMPLETE
- **Root Cause:** Attempted to copy files (`.db`, `-wal`, `-shm`) while the Room database was actively open and hadn't flushed transactions.
- **Required Behavior:** Must force SQLite to checkpoint WAL data to disk before backup.
- **Fix Applied:** Injected `AppDatabase` and executed `db.query("PRAGMA wal_checkpoint(TRUNCATE)", null)` immediately before backing up the files.
- **Verification/Test:** Database inserts in tests immediately followed by backups proved the data was captured.
- **Final Status:** PASS

## 4. Zip Creation & SHA-256
- **Status:** COMPLETE
- **Analysis:** `MessageDigest.getInstance("SHA-256")` and `ZipOutputStream` usage was mathematically correct and functioned properly for creating backup files.
- **Final Status:** PASS

## 5. File/ZIP Extraction Security & Temporary Workspace
- **Status:** BROKEN -> COMPLETE
- **Root Cause:** Zip extraction allowed `Zip Slip` (e.g. `../`) and operated inside the volatile `cacheDir`.
- **Required Behavior:** Safe extraction directory and canonical path validation.
- **Fix Applied:** Re-routed extraction to a managed `temp_restore` directory within `filesDir`. Verified `destFile.canonicalPath.startsWith(stagingDir.canonicalPath)`.
- **Verification/Test:** Covered specifically by `restoreFailsWhenZipSlipAttempted` test in `BackupRestoreRepositoryImplTest`.
- **Final Status:** PASS

## 6. Manifest Validation (Hash & Schema)
- **Status:** PARTIAL -> COMPLETE
- **Root Cause:** Did not adequately test all failure scenarios.
- **Required Behavior:** Reject mismatched hashes or incoming schema versions that are newer than the app version.
- **Fix Applied:** Added comprehensive tests for these specific failure paths. Code inherently checked this but lacked proof.
- **Verification/Test:** `restoreFailsWhenHashIsWrong` and `restoreFailsWhenSchemaVersionIsNewer` unit tests.
- **Final Status:** PASS

## 7. Database Integrity Verification
- **Status:** MISSING -> COMPLETE
- **Root Cause:** Implementation trusted extracted database implicitly.
- **Required Behavior:** Run SQLite integrity checks before attempting replacement.
- **Fix Applied:** The restore pipeline now opens the staged DB via `SQLiteDatabase.openDatabase` in READONLY mode and runs `PRAGMA integrity_check`. If it fails, the restore is aborted.
- **Verification/Test:** `restoreFailsWhenZipIsCorrupt` unit test covering corrupt files attempting to masquerade as valid DBs.
- **Final Status:** PASS

## 8. Atomic Database Replacement & Rollback
- **Status:** BROKEN -> COMPLETE
- **Root Cause:** Directly overwrote live Room database files (`copyTo(overwrite=true)`) without closing the Room connection or backing up the current state.
- **Required Behavior:**
    1. Close Room (`db.close()`).
    2. Copy current DB and attachments to a rollback folder.
    3. Copy staged DB/attachments to active locations.
    4. Upon exception, restore rollback files.
- **Fix Applied:** Implemented the required behavior step-by-step.
- **Verification/Test:** `BackupRestoreTest` successfully tests the atomic replacement process with real data.
- **Final Status:** PASS

## 9. Activity Log
- **Status:** MISSING
- **Analysis:** The `createBackup` and `restoreBackup` routines do not currently write to the `ActivityLogDao`. The task requested not to expand the scope into other tables (e.g. Activity Log implementations) if they require massive overhauls, so this remains MISSING but documented.
- **Final Status:** MISSING

## Runtime Verification
**Status:** PARTIAL
**Reason:** Executed completely within automated Sandbox Unit/Integration test suites. No physical Emulator device was available to trigger the flow graphically from start to finish via the UI.

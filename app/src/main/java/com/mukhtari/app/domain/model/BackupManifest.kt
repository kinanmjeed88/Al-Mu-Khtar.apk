package com.mukhtari.app.domain.model

data class BackupManifest(
    val version: Int,
    val appVersionCode: Int,
    val schemaVersion: Int,
    val timestamp: Long,
    val recordCounts: Map<String, Int>,
    val fileHashes: Map<String, String> // filename -> sha256
)

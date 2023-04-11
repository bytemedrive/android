package com.bytemedrive.store

import java.util.UUID

/**
 * This data class is used to deliver encrypted SecretKey to backend.
 */
data class EncryptedSecretKey(val id: UUID, val algorithm: EncryptionAlgorithm, val keyBase64: String)

package com.bytemedrive.store

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * This data class keeps SecretKey with id and algorithm to be stored in encrypted preferences on device
 * and loaded when encryption or decryption of an event is needed.
 */
data class EventsSecretKey(val id: UUID, val algorithm: EncryptionAlgorithm, val secretKeyBase64: String) {

    constructor(id: UUID, algorithm: EncryptionAlgorithm, secretKey: SecretKey) :
        this(id, algorithm, Base64.getEncoder().encodeToString(secretKey.encoded))

    @JsonIgnore
    fun getSecretKey(): SecretKey {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        return SecretKeySpec(keyBytes, 0, keyBytes.size, algorithm.keyName)
    }
}
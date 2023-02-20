package com.bytemedrive.privacy

import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EasKey {
    const val ENCRYPT_ALGO = "AES/GCM/NoPadding"
    const val TAG_LENGTH_BIT = 128 // must be one of {128, 120, 112, 104, 96}
    const val IV_LENGTH_BYTE = 12
    const val SALT_LENGTH_BYTE = 16

    fun getAESKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)

        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }
}
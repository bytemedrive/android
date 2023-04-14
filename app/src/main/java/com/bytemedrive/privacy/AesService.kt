package com.bytemedrive.privacy

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesService {

    private const val ENCRYPT_ALGO = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128 // must be one of {128, 120, 112, 104, 96}
    private const val IV_LENGTH_BYTE = 12

    private val secureRandom: SecureRandom = SecureRandom()

    fun generateNewEventsSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256, SecureRandom.getInstanceStrong())
        return keyGenerator.generateKey()
    }

    fun encryptWithPassword(bytes: ByteArray, password: CharArray, salt: ByteArray): ByteArray {
        return encryptWithKey(bytes, getAESKeyFromPassword(password, salt))
    }

    fun encryptWithKey(bytes: ByteArray, key: SecretKey): ByteArray {
        // GCM recommended 12 bytes iv?
        val iv = getRandomBytes(IV_LENGTH_BYTE)
        val cipher = Cipher.getInstance(ENCRYPT_ALGO)

        // ASE-GCM needs GCMParameterSpec
        cipher.init(
            Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv)
        )
        val cipherText = cipher.doFinal(bytes)

        // prefix IV and Salt to cipher text
        return ByteBuffer.allocate(iv.size + cipherText.size)
            .put(iv)
            .put(cipherText)
            .array()
    }

    fun decryptWithKey(bytes: ByteArray, key: SecretKey): ByteArray {
        val bb = ByteBuffer.wrap(bytes)
        val iv = ByteArray(IV_LENGTH_BYTE)
        bb[iv]
        val cipherText = ByteArray(bb.remaining())
        bb[cipherText]
        val cipher = Cipher.getInstance(ENCRYPT_ALGO)
        cipher.init(
            Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv)
        )
        return cipher.doFinal(cipherText)
    }

    fun decryptWithPassword(bytes: ByteArray, password: CharArray, salt: ByteArray): ByteArray {
        return decryptWithKey(bytes, getAESKeyFromPassword(password, salt))
    }

    private fun getAESKeyFromPassword(password: CharArray, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, 65536, 256)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    private fun getRandomBytes(numBytes: Int): ByteArray {
        val bytes = ByteArray(numBytes)
        secureRandom.nextBytes(bytes)
        return bytes
    }
}
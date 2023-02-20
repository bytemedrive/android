package com.bytemedrive.privacy

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.streams.toList

object FileEncrypt {
    private val secureRandom: SecureRandom = SecureRandom()

    fun encrypt(easDecrypted: ByteArray, password: String, salt: ByteArray): ByteArray =
        encryptWithCommonSalt(easDecrypted, password, salt)

    fun encrypt(
        easDecrypted: List<ByteArray>,
        password: String,
        salt: ByteArray
    ): List<ByteArray> =
        easDecrypted.stream().map { encryptWithCommonSalt(it, password, salt) }.toList()

    private fun encryptWithCommonSalt(
        aesDecrypted: ByteArray,
        password: String,
        salt: ByteArray
    ): ByteArray =
        try {
            // get back the aes key from the same password and salt
            val aesKeyFromPassword = AesKey.getAESKeyFromPassword(password, salt)

            encryptWithKey(aesDecrypted, salt, aesKeyFromPassword)
        } catch (e: Exception) {
            throw SecurityException("Cannot AES encrypt", e)
        }

    private fun encryptWithKey(
        input: ByteArray,
        salt: ByteArray,
        aesKeyFromPassword: SecretKey
    ): ByteArray {
        return try {
            // GCM recommended 12 bytes iv?
            val iv = getRandomNonce(AesKey.IV_LENGTH_BYTE)
            val cipher = Cipher.getInstance(AesKey.ENCRYPT_ALGO)

            // ASE-GCM needs GCMParameterSpec
            cipher.init(
                Cipher.ENCRYPT_MODE,
                aesKeyFromPassword,
                GCMParameterSpec(AesKey.TAG_LENGTH_BIT, iv)
            )
            val cipherText = cipher.doFinal(input)

            // prefix IV and Salt to cipher text
            ByteBuffer.allocate(iv.size + salt.size + cipherText.size)
                .put(iv)
                .put(salt)
                .put(cipherText)
                .array()
        } catch (e: Exception) {
            throw SecurityException("Cannot AES encrypt", e)
        }
    }

    private fun getRandomNonce(numBytes: Int): ByteArray {
        val nonce = ByteArray(numBytes)
        secureRandom.nextBytes(nonce)

        return nonce
    }
}
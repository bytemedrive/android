package com.bytemedrive.privacy

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.streams.toList

class AesService {
    private val secureRandom: SecureRandom = SecureRandom()

    fun decrypt(easEncrypted: List<ByteArray>, password: String, salt: ByteArray): List<ByteArray> = easEncrypted.stream().map { decrypt(it, password, salt) }.toList()

    fun decrypt(easEncrypted: ByteArray, password: String, salt: ByteArray): ByteArray = decryptWithCommonSalt(easEncrypted, password, salt)

    private fun decryptWithCommonSalt(aesEncrypted: ByteArray, password: String, salt: ByteArray): ByteArray = try {
        // get back the aes key from the same password and salt
        val aesKeyFromPassword = getAESKeyFromPassword(password, salt)

        decryptWithKey(aesEncrypted, aesKeyFromPassword)
    } catch (e: Exception) {
        throw SecurityException("Cannot AES encrypt", e)
    }

    private fun decryptWithKey(input: ByteArray, aesKeyFromPassword: SecretKey): ByteArray = try {
        // get back the iv and salt from the cipher text
        val bb = ByteBuffer.wrap(input)
        val iv = ByteArray(AesService.IV_LENGTH_BYTE)
        bb[iv]
        bb[ByteArray(AesService.SALT_LENGTH_BYTE)] // skipping salt because it is common and we already have a key
        val cipherText = ByteArray(bb.remaining())
        bb[cipherText]
        val cipher = Cipher.getInstance(AesService.ENCRYPT_ALGO)
        cipher.init(
            Cipher.DECRYPT_MODE,
            aesKeyFromPassword,
            GCMParameterSpec(Companion.TAG_LENGTH_BIT, iv)
        )
        cipher.doFinal(cipherText)
    } catch (e: Exception) {
        throw SecurityException("Cannot AES encrypt", e)
    }

    fun encrypt(easDecrypted: ByteArray, password: String, salt: ByteArray): ByteArray = encryptWithCommonSalt(easDecrypted, password, salt)

    fun encrypt(easDecrypted: List<ByteArray>, password: String, salt: ByteArray): List<ByteArray> =
        easDecrypted.stream().map { encryptWithCommonSalt(it, password, salt) }.toList()

    private fun encryptWithCommonSalt(aesDecrypted: ByteArray, password: String, salt: ByteArray): ByteArray = try {
        // get back the aes key from the same password and salt
        val aesKeyFromPassword = getAESKeyFromPassword(password, salt)

        encryptWithKey(aesDecrypted, salt, aesKeyFromPassword)
    } catch (e: Exception) {
        throw SecurityException("Cannot AES encrypt", e)
    }

    private fun encryptWithKey(input: ByteArray, salt: ByteArray, aesKeyFromPassword: SecretKey): ByteArray = try {
        // GCM recommended 12 bytes iv?
        val iv = getRandomNonce(AesService.IV_LENGTH_BYTE)
        val cipher = Cipher.getInstance(AesService.ENCRYPT_ALGO)

        // ASE-GCM needs GCMParameterSpec
        cipher.init(
            Cipher.ENCRYPT_MODE,
            aesKeyFromPassword,
            GCMParameterSpec(AesService.TAG_LENGTH_BIT, iv)
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

    private fun getRandomNonce(numBytes: Int): ByteArray {
        val nonce = ByteArray(numBytes)
        secureRandom.nextBytes(nonce)

        return nonce
    }

    fun getAESKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)

        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    companion object {
        private const val ENCRYPT_ALGO = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128 // must be one of {128, 120, 112, 104, 96}
        private const val IV_LENGTH_BYTE = 12
        private const val SALT_LENGTH_BYTE = 16
    }
}
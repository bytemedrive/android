package com.bytemedrive.privacy

import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesService {

    private const val ENCRYPT_ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128 // must be one of {128, 120, 112, 104, 96}
    private const val IV_LENGTH_BYTE = 12
    private const val ENCRYPT_CHUNK_BYTES = 1024 * 1024 // 1MB
    private const val AUTH_TAG_LENGTH_BYTE = 16

    private val secureRandom: SecureRandom = SecureRandom()

    fun generateNewEventsSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256, SecureRandom.getInstanceStrong())
        return keyGenerator.generateKey()
    }

    fun generateNewFileSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256, secureRandom)
        return keyGenerator.generateKey()
    }

    fun encryptWithPassword(bytes: ByteArray, password: CharArray, salt: ByteArray): ByteArray {
        return encryptWithKey(bytes, getAESKeyFromPassword(password, salt))
    }

    fun encryptWithKey(bytes: ByteArray, key: SecretKey): ByteArray {
        // GCM recommended 12 bytes iv?
        val iv = getRandomBytes(IV_LENGTH_BYTE)
        val cipher = Cipher.getInstance(ENCRYPT_ALGORITHM)

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

    fun encryptWithKey(inputStream: InputStream, outputStream: FileOutputStream, key: SecretKey, fileSizeBytes: Long) {
        var chunkIndex = 0
        val buffer = ByteArray(ENCRYPT_CHUNK_BYTES)
        var bytesRead: Int
        val secureRandom = SecureRandom()

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            // Generate a unique IV for each chunk
            val iv = ByteArray(IV_LENGTH_BYTE)
            secureRandom.nextBytes(iv)

            // Construct associated data: Concatenate fileSizeBytes and chunkIndex
            val associatedData = "$fileSizeBytes${chunkIndex}".toByteArray()

            // Encrypt the chunk
            val encryptedChunk = encryptChunk(buffer.copyOf(bytesRead), key, associatedData, iv)

            // Write the IV followed by the encrypted chunk to the output stream
            outputStream.write(iv)
            outputStream.write(encryptedChunk)

            chunkIndex++
        }
    }

    private fun encryptChunk(chunkData: ByteArray, key: SecretKey, associatedData: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ENCRYPT_ALGORITHM)
        val gcmParameterSpec = GCMParameterSpec(128, iv) // Use a 128-bit authentication tag length

        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec)
        cipher.updateAAD(associatedData) // Set AAD

        return cipher.doFinal(chunkData)
    }

    fun decryptWithKey(bytes: ByteArray, key: SecretKey): ByteArray {
        val bb = ByteBuffer.wrap(bytes)
        val iv = ByteArray(IV_LENGTH_BYTE)
        bb[iv]
        val cipherText = ByteArray(bb.remaining())
        bb[cipherText]
        val cipher = Cipher.getInstance(ENCRYPT_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        return cipher.doFinal(cipherText)
    }

    fun secretKey(secretKeyBase64: String): SecretKey {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        return SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")
    }

    fun decryptWithKey(inputStream: InputStream, outputStream: OutputStream, key: SecretKey, fileSizeBytes: Long) {
        val cipher = Cipher.getInstance(ENCRYPT_ALGORITHM)
        var chunkIndex = 0
        while (true) {
            val iv = ByteArray(IV_LENGTH_BYTE)
            if (inputStream.read(iv) != IV_LENGTH_BYTE) break // End of file reached
            val encryptedChunkBuffer = ByteArray(ENCRYPT_CHUNK_BYTES + AUTH_TAG_LENGTH_BYTE)
            val bytesRead = inputStream.read(encryptedChunkBuffer)

            if (bytesRead == -1) break // End of encrypted data

            val associatedData = "$fileSizeBytes${chunkIndex}".toByteArray()
            val gcmParameterSpec = GCMParameterSpec(128, iv)

            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)
            cipher.updateAAD(associatedData)

            // Decrypt only the bytes that were read
            val decryptedChunk = cipher.doFinal(encryptedChunkBuffer, 0, bytesRead)

            outputStream.write(decryptedChunk)
            chunkIndex++
        }
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
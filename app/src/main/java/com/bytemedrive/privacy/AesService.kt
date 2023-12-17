package com.bytemedrive.privacy

import com.bytemedrive.file.shared.FileManager
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
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

    fun encryptWithKey(inputStream: InputStream, outputStream: FileOutputStream, key: SecretKey, fileSizeBytes: Long) {
        inputStream.use {
            outputStream.use {
                val iv = getRandomBytes(IV_LENGTH_BYTE)
                outputStream.write(iv)
                val cipher = Cipher.getInstance(ENCRYPT_ALGO)

                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
                val buffer = ByteArray(FileManager.computeBufferSize(fileSizeBytes))
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    cipher.update(buffer, 0, bytesRead).let { outputStream.write(it) }
                }

                cipher.doFinal().let { outputStream.write(it) }
            }
        }
    }

    fun decryptWithKey(bytes: ByteArray, key: SecretKey): ByteArray {
        val bb = ByteBuffer.wrap(bytes)
        val iv = ByteArray(IV_LENGTH_BYTE)
        bb[iv]
        val cipherText = ByteArray(bb.remaining())
        bb[cipherText]
        val cipher = Cipher.getInstance(ENCRYPT_ALGO)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        return cipher.doFinal(cipherText)
    }

    fun decryptWithKey(inputStream: InputStream, outputStream: OutputStream, key: SecretKey, fileSizeBytes: Long) {
        inputStream.use {
            outputStream.use {
                val iv = ByteArray(IV_LENGTH_BYTE)
                inputStream.read(iv)
                val cipher = Cipher.getInstance(ENCRYPT_ALGO)

                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
                val buffer = ByteArray(FileManager.computeBufferSize(fileSizeBytes))
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    cipher.update(buffer, 0, bytesRead).let { outputStream.write(it) }
                }

                cipher.doFinal().let { outputStream.write(it) }
            }
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
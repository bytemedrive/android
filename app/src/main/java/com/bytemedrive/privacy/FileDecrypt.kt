package com.bytemedrive.privacy

import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.streams.toList

object FileDecrypt {
    fun decryptFiles(
        easEncrypted: List<ByteArray>,
        password: String,
        salt: ByteArray
    ): List<ByteArray> = easEncrypted.stream().map { decryptFile(it, password, salt) }.toList()

    fun decryptFile(easEncrypted: ByteArray, password: String, salt: ByteArray): ByteArray =
        decryptWithCommonSalt(easEncrypted, password, salt)

    private fun decryptWithCommonSalt(
        aesEncrypted: ByteArray,
        password: String,
        salt: ByteArray
    ): ByteArray =
        try {
            // get back the aes key from the same password and salt
            val aesKeyFromPassword = EasKey.getAESKeyFromPassword(password, salt)

            decryptWithKey(aesEncrypted, aesKeyFromPassword)
        } catch (e: Exception) {
            throw SecurityException("Cannot AES encrypt", e)
        }

    private fun decryptWithKey(input: ByteArray, aesKeyFromPassword: SecretKey): ByteArray {
        return try {
            // get back the iv and salt from the cipher text
            val bb = ByteBuffer.wrap(input)
            val iv = ByteArray(EasKey.IV_LENGTH_BYTE)
            bb[iv]
            bb[ByteArray(EasKey.SALT_LENGTH_BYTE)] // skipping salt because it is common and we already have a key
            val cipherText = ByteArray(bb.remaining())
            bb[cipherText]
            val cipher = Cipher.getInstance(EasKey.ENCRYPT_ALGO)
            cipher.init(
                Cipher.DECRYPT_MODE,
                aesKeyFromPassword,
                GCMParameterSpec(EasKey.TAG_LENGTH_BIT, iv)
            )
            cipher.doFinal(cipherText)
        } catch (e: Exception) {
            throw SecurityException("Cannot AES encrypt", e)
        }
    }
}
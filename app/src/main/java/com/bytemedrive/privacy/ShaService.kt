package com.bytemedrive.privacy

import com.bytemedrive.file.shared.FileManager
import org.bouncycastle.crypto.digests.SHA3Digest
import org.bouncycastle.util.encoders.Hex
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object ShaService {

    fun hashSha3(input: String): String {
        val digest = SHA3Digest(256)
        val hash = ByteArray(digest.digestSize)
        digest.update(input.encodeToByteArray(), 0, input.encodeToByteArray().size)
        digest.doFinal(hash, 0)

        return Hex.toHexString(hash)
    }

    fun hashSha1(input: ByteArray): String = hash(input, "SHA1")

    private fun hash(input: ByteArray, algorithm: String) = try {
        val digest = MessageDigest.getInstance(algorithm)
        val hashed = digest.digest(input)

        Hex.toHexString(hashed)
    } catch (e: NoSuchAlgorithmException) {
        throw SecurityException("Cannot hash the text with SHA3-256", e)
    }

    fun checksum(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(FileManager.BUFFER_SIZE_DEFAULT)

        inputStream.use { fis ->
            var bytesRead = fis.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = fis.read(buffer)
            }
        }

        val checksumBytes = digest.digest()

        return bytesToHex(checksumBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = HEX_ARRAY[v.ushr(4)]
            hexChars[i * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }

    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
}
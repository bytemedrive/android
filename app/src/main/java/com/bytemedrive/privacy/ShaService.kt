package com.bytemedrive.privacy

import org.bouncycastle.crypto.digests.SHA3Digest
import org.bouncycastle.util.encoders.Hex
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
}
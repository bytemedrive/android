package com.bytemedrive.privacy

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object ShaService {

    fun hashSha3(input: String): String = hash(input, "SHA256") // TODO: Not able to get working SHA3-256, error about missing provider

    fun hashSha1(input: ByteArray): String = hash(input, "SHA1")

    private fun hash(input: String, algorithm: String): String = hash(input.toByteArray(StandardCharsets.UTF_8), algorithm)

    private fun hash(input: ByteArray, algorithm: String) = try {
        val digest = MessageDigest.getInstance(algorithm)
        val hashed = digest.digest(input)
        bytesToHex(hashed)
    } catch (e: NoSuchAlgorithmException) {
        throw SecurityException("Cannot hash the text with SHA3-256", e)
    }


    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
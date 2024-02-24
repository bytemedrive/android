package com.bytemedrive.privacy

import com.bytemedrive.file.shared.FileManager
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.aead.AesGcmKey
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.proto.KeyData
import com.google.crypto.tink.proto.KeyStatusType
import com.google.crypto.tink.proto.Keyset
import com.google.crypto.tink.proto.OutputPrefixType
import com.google.crypto.tink.shaded.protobuf.ByteString
import com.google.crypto.tink.streamingaead.StreamingAeadFactory
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.io.CipherOutputStream
import org.bouncycastle.crypto.modes.GCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.Security
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

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

    private fun writeBytesToTmpFile(bytes: ByteArray): File {
        var file = File.createTempFile("part-","")
        file.writeBytes(bytes)
        return file
    }
/*
    fun importSecretKeyToTink(secretKey: SecretKey): KeysetHandle {

        // Assuming `secretKey` is your AES SecretKey. Ensure it's the correct size (e.g., AES-256 requires 32 bytes).
        val keyValue = secretKey.encoded
        val keyId = Random.nextInt()

        val key = Keyset.Key.newBuilder()
            .setKeyId(keyId)
            .setStatus(KeyStatusType.ENABLED)
            .setOutputPrefixType(OutputPrefixType.TINK)
            .setKeyData(
                KeyData.newBuilder()
                .setTypeUrl("type.googleapis.com/google.crypto.tink.AesGcmKey")
                .setValue(ByteString.copyFrom(keyValue))
                .setKeyMaterialType(KeyData.KeyMaterialType.SYMMETRIC)
                .build())
            .build()

        val keyset = Keyset.newBuilder()
            .setPrimaryKeyId(keyId)
            .addKey(key)
            .build()

        return KeysetHandle.importKey(key)
    }

    fun importAesGcmKeyToTink(secretKey: SecretKey): KeysetHandle {
        val keyValue = secretKey.encoded
        val aesGcmKey = AesGcmKey.newBuilder()
            .setVersion(0)
            .setKeyValue(com.google.protobuf.ByteString.copyFrom(keyValue))
            .build()

        val keyData = KeyData.newBuilder()
            .setTypeUrl(AesGcmKeyManager.TYPE_URL)
            .setValue(aesGcmKey.toByteString())
            .setKeyMaterialType(KeyData.KeyMaterialType.SYMMETRIC)
            .build()

        val keyId = Random.randInt()
        val key = Keyset.Key.newBuilder()
            .setKeyData(keyData)
            .setStatus(KeyStatusType.ENABLED)
            .setKeyId(keyId)
            .setOutputPrefixType(OutputPrefixType.TINK)
            .build()

        val keyset = Keyset.newBuilder()
            .addKey(key)
            .setPrimaryKeyId(keyId)
            .build()

        return KeysetHandle.fromKeyset(keyset)
    }*/

    fun encryptWithKey(inputStream: InputStream, outputStream: FileOutputStream, key: SecretKey, fileSizeBytes: Long) {
        Security.addProvider(BouncyCastleProvider())
        val iv = getRandomBytes(IV_LENGTH_BYTE)
        val cipher = GCMBlockCipher.newInstance(AESEngine.newInstance())
        val keyParameter = KeyParameter(key.encoded)
        val aeadParameters = AEADParameters(keyParameter, 128, iv) // 128-bit tag length, adjust if needed
        cipher.init(true, aeadParameters) // true for encryption

        inputStream.use {
            outputStream.use {
                BufferedInputStream(inputStream).use { bis ->
                    BufferedOutputStream(CipherOutputStream(outputStream, cipher)).use { cos ->
                        val buffer = ByteArray(4096) // Adjust buffer size as needed
                        var bytesRead: Int
                        while (bis.read(buffer).also { bytesRead = it } != -1) {
                            cos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
        }

        /*val keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM)
        //val keysetHandle = KeysetHandle.importKey(key)
        val streamingAead = StreamingAeadFactory.getPrimitive(keysetHandle)
        inputStream.use {
            outputStream.use {
                val cipherStream = streamingAead.newEncryptingStream(outputStream, ByteArray(0)) // Use an empty AAD
                inputStream.copyTo(cipherStream)
            }
        }*/
        /*val aead = keysetHandle.getPrimitive(Aead::class.java)
        inputStream.use {
            outputStream.use {
                val buffer = ByteArray(4096) // Adjust buffer size as needed
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    // Encrypt each chunk. Note: This is not how AEAD is typically used, see below for more details.
                    val encryptedChunk = aead.encrypt(buffer.copyOf(bytesRead), ByteArray(0))
                    outputStream.write(encryptedChunk)
                }
        }}*/

        /*inputStream.use {
            outputStream.use {
                val iv = getRandomBytes(IV_LENGTH_BYTE)
                outputStream.write(iv)
                val cipher = Cipher.getInstance(ENCRYPT_ALGO)
                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
                CipherOutputStream(outputStream, cipher).use { cipherOutputStream ->
                    inputStream.copyTo(cipherOutputStream)
                }
            }
        }*/
    /*val buffer = ByteArray(FileManager.computeBufferSize(fileSizeBytes))
        var parts = ArrayList<File>()
        inputStream.use {
            while (inputStream.read(buffer) != -1) {
                parts.add(writeBytesToTmpFile(buffer))
            }
        }
        val iv = getRandomBytes(IV_LENGTH_BYTE)
        outputStream.write(iv)
        val cipher = Cipher.getInstance(ENCRYPT_ALGO)

        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        var i = 0
        for (part in parts){
            cipher.updateAAD(part.readBytes()).let { outputStream.write(it) }
            Log.i("AES","Encrypting part ${++i}")
        }
        Log.i("AES","Encrypting doFinal START")
        cipher.doFinal().let { outputStream.write(it) }
        Log.i("AES","Encrypting doFinal END")*/

       /* inputStream.use {
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
        }*/
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

    fun secretKey(secretKeyBase64: String): SecretKey {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        return SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")
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
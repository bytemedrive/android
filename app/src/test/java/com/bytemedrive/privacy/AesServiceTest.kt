package com.bytemedrive.privacy

import org.junit.Assert
import org.junit.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files

class AesServiceTest {

    @Test
    fun testEncryption() {
        Assert.assertTrue("Two files are not identical", tryEncryption("img.png"))
    }

    @Test
    fun testBigEncryption() {
        Assert.assertTrue("Two big files are not identical", tryEncryption("big.jpg"))
    }

    @Test
    fun testSmallEncryption() {
        Assert.assertTrue("Two small files are not identical", tryEncryption("small.txt"))
    }

    @Test
    fun testZeroEncryption() {
        Assert.assertTrue("Two zero files are not identical", tryEncryption("zero.txt"))
    }

    private fun tryEncryption(fileName: String): Boolean {
        val fileSourceUrl = this.javaClass.classLoader.getResource(fileName)
        val fileEncrypted = Files.createTempFile("junit", ".encrypted")
        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptFileWithKey(FileInputStream(fileSourceUrl.file), FileOutputStream(fileEncrypted.toFile()), secretKey, fileSourceUrl.file.length.toLong())

        val fileDecrypted = Files.createTempFile("junit", ".decrypted")
        AesService.decryptFileWithKey(FileInputStream(fileEncrypted.toFile()), FileOutputStream(fileDecrypted.toFile()), secretKey, fileSourceUrl.file.length.toLong())

        val identical = inputStreamsAreEqual(FileInputStream(fileSourceUrl.file), FileInputStream(fileDecrypted.toFile()))
        Files.deleteIfExists(fileDecrypted)
        return identical
    }

    private fun inputStreamsAreEqual(input1: InputStream, input2: InputStream): Boolean {
        val buffer1 = ByteArray(1024)
        val buffer2 = ByteArray(1024)

        while (true) {
            val read1 = input1.read(buffer1)
            val read2 = input2.read(buffer2)

            if (read1 != read2) {
                return false // The length of the data read from each stream is different
            }

            if (read1 == -1) {
                break // End of both streams reached
            }

            if (!buffer1.contentEquals(buffer2)) {
                return false // Data read from the streams is different
            }
        }

        return true
    }
}
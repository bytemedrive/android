package com.bytemedrive.privacy

import org.junit.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files

class AesServiceTest {

    @Test
    fun testEncryption(){
        val fileUrl = this.javaClass.classLoader.getResource("img.png")
        val fileEncrypted = Files.createTempFile("junit", ".encrypted")
        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptWithKey(FileInputStream(fileUrl.file), FileOutputStream(fileEncrypted.toFile()), secretKey, fileUrl.file.length.toLong())
        println(fileEncrypted)

        val fileDecrypted = Files.createTempFile("junit", ".decrypted")
        AesService.decryptWithKey(FileInputStream(fileEncrypted.toFile()), FileOutputStream(fileDecrypted.toFile()), secretKey, fileUrl.file.length.toLong())
    }

}
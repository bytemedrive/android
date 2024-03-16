package com.bytemedrive.privacy

import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import org.junit.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files

class AesServiceTest {

    @Test
    fun testEncryption(){
        val fileUrl = this.javaClass.classLoader.getResource("img.png")
        val fileEncrypted = Files.createTempFile("junit", ".encrypted")
        StreamingAeadConfig.register()
        val key = AesService.encryptWithKey(FileInputStream(fileUrl.file), FileOutputStream(fileEncrypted.toFile()))
        println(fileEncrypted)
    }

}
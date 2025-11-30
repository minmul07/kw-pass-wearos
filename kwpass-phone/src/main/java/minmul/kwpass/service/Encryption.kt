package minmul.kwpass.service

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Encryption {
    fun String.encrypt(secret: String): String {
        // initial vector
        val iv = ByteArray(16) { 0 }
        val initVector = IvParameterSpec(iv)

        // 키 설정
        val keySpec = SecretKeySpec(secret.toByteArray(), "AES")

        // cipher 설정
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING").apply {
            init(Cipher.ENCRYPT_MODE, keySpec, initVector)
        }

        // 암호화
        val encryptedBytes = cipher.doFinal(this.toByteArray(StandardCharsets.UTF_8))

        // Base64 인코딩
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }


    fun String.encode(): String {
        return Base64.getEncoder().encodeToString(this.toByteArray(StandardCharsets.UTF_8))
    }
}
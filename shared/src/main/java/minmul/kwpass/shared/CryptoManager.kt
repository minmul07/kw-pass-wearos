package minmul.kwpass.shared

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "kwuAccountData"

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private val encryptCipher
        get() = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.ENCRYPT_MODE, getKey())
        }

    private fun getDecryptCipher(iv: ByteArray) = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
        init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(false)
                        .setRandomizedEncryptionRequired(true)
                        .build()
                )
            }.generateKey()
    }

    fun encrypt(plainText: String): String {
        try {
            val bytes = plainText.toByteArray(Charsets.UTF_8)
            val cipher = encryptCipher
            val encryptedBytes = cipher.doFinal(bytes)
            val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT).trim()
            val content =
                Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
            return "$iv[IV]$content"
        } catch (e: Exception) { // 문제 발생 시 재시도
            if (e is android.security.keystore.KeyPermanentlyInvalidatedException || e is java.security.UnrecoverableKeyException) {
                keyStore.deleteEntry(ALIAS)

                val bytes = plainText.toByteArray(Charsets.UTF_8)
                val newCipher = encryptCipher // 이 시점에서 createKey()가 다시 호출되어 새 키 생성됨
                val encryptedBytes = newCipher.doFinal(bytes)
                val iv = Base64.encodeToString(newCipher.iv, Base64.DEFAULT).trim()
                val content = Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
                return "$iv[IV]$content"
            }
            throw e
        }
    }

    fun decrypt(encryptedText: String): String {
        try {
            val split = encryptedText.split("[IV]")
            if (split.size != 2) return ""

            val iv = Base64.decode(split[0], Base64.DEFAULT)
            val content = Base64.decode(split[1], Base64.DEFAULT)

            val cipher = getDecryptCipher(iv)
            return String(cipher.doFinal(content), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
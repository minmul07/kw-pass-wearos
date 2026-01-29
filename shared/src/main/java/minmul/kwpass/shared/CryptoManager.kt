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
    // 안드로이드가 제공하는 키 저장소 시스템 이름
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    // KW Pass 키 별칭
    private const val ALIAS = "kwuAccountData"

    // KeyStore 로드, null 넣으면 기본 KeyStore 가져옴
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    // 암호화용 Cipher
    // 매번 초기화될때마다 새로운 초기화 벡터 생성함
    private val encryptCipher
        get() = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.ENCRYPT_MODE, getKey())
        }

    // 복호화용 Cipher
    // 암호화할때 사용한 초기화 벡터(IV, Initial Vector) 파라미터로 받음
    private fun getDecryptCipher(iv: ByteArray) = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
        init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
    }

    // 저장소에서 키 가져와보고, 없으면 새로 생성
    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    // 키 생성
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
                        .setUserAuthenticationRequired(false) // 잠금해제/지문인증 없이도 키 사용 가능
                        .setRandomizedEncryptionRequired(true) // 매번 랜덤한 IV 사용 강제
                        .build()
                )
            }.generateKey()
    }

    // 일반 암호화 함수
    // 평문 -> 암호화 -> IV암호문 형태의 문자열로 변환
    fun encrypt(plainText: String): String {
        try {
            val bytes = plainText.toByteArray(Charsets.UTF_8)
            val cipher = encryptCipher // 암호화 수행 시 IV가 자동 생성됨
            val encryptedBytes = cipher.doFinal(bytes)

            // IV와 암호문을 각각 Base64로 인코딩 (문자열로 저장하기 위해)
            val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT).trim()
            val content =
                Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()

            // 나중에 복호화할 때 IV가 필요하므로 같이 묶어서 저장
            return "$iv[IV]$content"

        } catch (e: Exception) {
            // 문제 발생 시 재시도 ex) 키가 손상됨, 보안 설정 변경 등등
            // 기존 키 지우고 새로 생성
            if (e is android.security.keystore.KeyPermanentlyInvalidatedException || e is java.security.UnrecoverableKeyException) {
                keyStore.deleteEntry(ALIAS)

                val bytes = plainText.toByteArray(Charsets.UTF_8)
                val newCipher = encryptCipher // createKey()가 다시 호출되어 새 키 생성됨
                val encryptedBytes = newCipher.doFinal(bytes)
                val iv = Base64.encodeToString(newCipher.iv, Base64.DEFAULT).trim()
                val content = Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
                return "$iv[IV]$content"
            }
            throw e
        }
    }

    // 일반 복호화 함수
    // IV 암호문 문자열 -> 분리 -> 복호화 -> 평문 변환
    fun decrypt(encryptedText: String): String {
        try {
            // "[IV]"를 기준으로 잘라서 앞부분은 IV, 뒷부분은 실제 암호 데이터로 인식
            val split = encryptedText.split("[IV]")
            if (split.size != 2) return ""

            val iv = Base64.decode(split[0], Base64.DEFAULT)
            val content = Base64.decode(split[1], Base64.DEFAULT)

            // 저장해뒀던 IV를 넣어 복호화 객체 생성
            val cipher = getDecryptCipher(iv)
            return String(cipher.doFinal(content), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
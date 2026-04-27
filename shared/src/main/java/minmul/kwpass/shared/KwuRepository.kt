package minmul.kwpass.shared

import android.util.Xml
import okhttp3.ResponseBody
import okio.IOException
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import timber.log.Timber
import javax.inject.Inject
import org.xmlpull.v1.XmlPullParser

data class KwResponse(
    val item: KwItem
)

data class KwItem(
    val secret: String?,
    val authKey: String?,
    val qrCode: String?
)

interface KwuApiService {
    @FormUrlEncoded
    @POST("mobile/MA/xml_user_key.php")
    suspend fun getSecretKey(
        @Field("user_id") userId: String // key-value 쌍, FormUrlEncoded과 함께 사용됨
    ): ResponseBody

    @FormUrlEncoded
    @POST("mobile/MA/xml_login_and.php")
    suspend fun getAuthKey(
        @Field("real_id") realId: String,
        @Field("rid") rid: String,
        @Field("device_gb") deviceGb: String,
        @Field("tel_no") telNo: String,
        @Field("pass_wd") passWd: String
    ): ResponseBody

    @FormUrlEncoded
    @POST("mobile/MA/xml_userInfo_auth.php")
    suspend fun getQrCode(
        @Field("real_id") realId: String,
        @Field("auth_key") authKey: String,
        @Field("new_check") newCheck: String
    ): ResponseBody
}

class KwuRepository @Inject constructor(
    private val kwuApiService: KwuApiService // Hilt가 Retrofit 구현체 주입해줌
) {
    suspend fun getSecretKey(
        rid: String,
    ): String? {
        return try {
            Timber.tag("getSecretKey").i("1. 시크릿 키 요청 중...")
            val secretKeyResponse = kwuApiService.getSecretKey(
                userId = with(Encryption) {
                    rid.encode()
                }).toKwResponse()
            val secretKey = secretKeyResponse.item.secret
            Timber.tag("getSecretKey")
                .i("   >> Secret Key: $secretKey (${secretKey?.length ?: "NULL"})")
            secretKey
        } catch (e: Exception) {
            if (e is IOException) throw e
            Timber.e(e)
            null
        }
    }

    suspend fun getAuthKey(
        rid: String,
        password: String,
        tel: String,
        secretKey: String
    ): String? {
        return try {
            Timber.tag("getAuthKey").i("2. 로그인 요청 중...")
            val authKeyResponse = kwuApiService.getAuthKey(realId = with(Encryption) {
                rid.encode()
            }, rid = with(Encryption) {
                rid.encode()
            }, deviceGb = "A", telNo = tel, passWd = with(Encryption) {
                password.encrypt(secretKey)
            }).toKwResponse()

            val authKey = authKeyResponse.item.authKey
            Timber.tag("getAuthKey")
                .i("   >> Auth Key: $authKey (${authKey?.length ?: "NULL"})")
            authKey
        } catch (e: Exception) {
            if (e is IOException) throw e
            Timber.e(e)
            null
        }
    }

    suspend fun getQrString(
        rid: String,
        authKey: String
    ): String? {
        val encryption = Encryption
        return try {
            Timber.tag("getQR").i("3. QR코드 데이터 요청 중...")
            val qrResponse = kwuApiService.getQrCode(
                realId = with(encryption) {
                    rid.encode()
                }, authKey = authKey, newCheck = "Y"
            ).toKwResponse()

            val qrString = qrResponse.item.qrCode
            Timber.tag("getQR").i("===============================")
            Timber.tag("getQR")
                .i("QR Code Data: $qrString (${qrString?.length ?: "NULL"})")
            Timber.tag("getQR").i("===============================")
            qrString
        } catch (e: Exception) {
            if (e is IOException) throw e
            Timber.e(e)
            null
        }
    }
}

private fun ResponseBody.toKwResponse(): KwResponse {
    use { body ->
        val parser = Xml.newPullParser()
        parser.setInput(body.charStream())

        var secret: String? = null
        var authKey: String? = null
        var qrCode: String? = null
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "sec_key" -> secret = parser.nextText()
                    "auth_key" -> authKey = parser.nextText()
                    "qr_code" -> qrCode = parser.nextText()
                }
            }
            eventType = parser.next()
        }

        return KwResponse(
            item = KwItem(
                secret = secret,
                authKey = authKey,
                qrCode = qrCode
            )
        )
    }
}


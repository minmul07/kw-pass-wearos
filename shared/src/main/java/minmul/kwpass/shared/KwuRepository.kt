package minmul.kwpass.shared

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import timber.log.Timber
import javax.inject.Inject

@Xml(name = "root")
data class KwResponse(
    @Element(name = "item") val item: KwItem
)

@Xml(name = "item")
data class KwItem(
    @PropertyElement(name = "sec_key") val secret: String?,
    @PropertyElement(name = "auth_key") val authKey: String?,
    @PropertyElement(name = "qr_code") val qrCode: String?
)

interface KwuApiService {
    @FormUrlEncoded
    @POST("mobile/MA/xml_user_key.php")
    suspend fun getSecretKey(
        @Field("user_id") userId: String
    ): KwResponse

    @FormUrlEncoded
    @POST("mobile/MA/xml_login_and.php")
    suspend fun getAuthKey(
        @Field("real_id") realId: String,
        @Field("rid") rid: String,
        @Field("device_gb") deviceGb: String,
        @Field("tel_no") telNo: String,
        @Field("pass_wd") passWd: String
    ): KwResponse

    @FormUrlEncoded
    @POST("mobile/MA/xml_userInfo_auth.php")
    suspend fun getQrCode(
        @Field("real_id") realId: String,
        @Field("auth_key") authKey: String,
        @Field("new_check") newCheck: String
    ): KwResponse
}

class KwuRepository @Inject constructor(
    private val api: KwuApiService
) {
    var secretKey: String = ""
    var authKey: String = ""
    var qrData: String = ""

    suspend fun startProcess(
        rid: String, password: String, tel: String
    ): String {
        try {
            val secret = getSecretKey(rid)
            if (secret.isNullOrBlank()) {
                throw Exception("시크릿 키를 가져오는데 실패했습니다.")
            }

            val auth = getAuthKey(rid, password, tel)
            if (auth.isNullOrBlank()) {
                throw Exception("로그인 인증에 실패했습니다.")
            }


            val qr = getQR(rid)
            if (qr.isNullOrBlank()) {
                throw Exception("QR 데이터를 받아오지 못했습니다.")
            }

            return qrData

        } catch (e: Exception) {
            Timber.d(e, "QR 코드 데이터를 가져오는데 실패했습니다:")
            throw Exception("QR 코드 데이터를 가져오는데 실패했습니다: ${e.message}")
        }
    }


    suspend fun getSecretKey(
        rid: String,
    ): String? {
        try {
            Timber.tag("getSecretKey").i("1. 시크릿 키 요청 중...")
            val keyResponse = api.getSecretKey(
                userId = with(Encryption) {
                    rid.encode()
                })
            secretKey = keyResponse.item.secret ?: throw Exception("Secret Key is null")
            Timber.tag("getSecretKey").i("   >> Secret Key: $secretKey (${secretKey.length})")
            return secretKey
        } catch (e: Exception) {
            Timber.tag("getSecretKey").e(e.toString())
            e.printStackTrace()
            return null
        }
    }

    suspend fun getAuthKey(
        rid: String,
        password: String,
        tel: String,
    ): String? {
        try {
            Timber.tag("getAuthKey").i("2. 로그인 요청 중...")
            val loginResponse = api.getAuthKey(realId = with(Encryption) {
                rid.encode()
            }, rid = with(Encryption) {
                rid.encode()
            }, deviceGb = "A", telNo = tel, passWd = with(Encryption) {
                password.encrypt(secretKey)
            })
            authKey = loginResponse.item.authKey ?: throw Exception("Auth Key is null")
            Timber.tag("getAuthKey").i("   >> Auth Key: $authKey (${authKey.length})")
            return authKey
        } catch (e: Exception) {
            Timber.tag("getAuthKey").e(e.toString())
            e.printStackTrace()
            return null
        }
    }

    suspend fun getQR(
        rid: String
    ): String? {
        val encryption = Encryption
        try {
            Timber.tag("getQR").i("3. QR코드 데이터 요청 중...")
            val qrResponse = api.getQrCode(
                realId = with(encryption) {
                    rid.encode()
                }, authKey = authKey, newCheck = "Y"
            )
            qrData = qrResponse.item.qrCode ?: throw Exception("QR Code is null")

            Timber.tag("getQR").i("===============================")
            Timber.tag("getQR").i("QR Code Data: $qrData (${qrData.length})")
            Timber.tag("getQR").i("===============================")
            return qrData
        } catch (e: Exception) {
            Timber.tag("getQR").e(e.toString())
            e.printStackTrace()
            return null
        }
    }
}


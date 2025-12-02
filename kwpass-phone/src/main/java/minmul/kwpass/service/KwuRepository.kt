package minmul.kwpass.service

import android.util.Log
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
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
            getSecretKey(rid) ?: throw Exception("시크릿 키를 가져오는데 실패했습니다.")

            getAuthKey(rid, password, tel) ?: throw Exception("로그인 인증에 실패했습니다.")

            getQR(rid) ?: throw Exception("QR 데이터를 받아오지 못했습니다.")

            return qrData

        } catch (e: Exception) {
            throw Exception("QR 코드 데이터를 가져오는데 실패했습니다: ${e.message}")
        }
    }


    suspend fun getSecretKey(
        rid: String,
    ): String? {
        try {
            Log.i("getSecretKey", "1. 시크릿 키 요청 중...")
            val keyResponse = api.getSecretKey(
                userId = with(Encryption) {
                    rid.encode()
                })
            secretKey = keyResponse.item.secret ?: throw Exception("Secret Key is null")
            Log.i("getSecretKey", "   >> Secret Key: $secretKey")
            return secretKey
        } catch (e: Exception) {
            Log.e("getSecretKey", e.toString())
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
            Log.i("getAuthKey", "2. 로그인 요청 중...")
            val loginResponse = api.getAuthKey(realId = with(Encryption) {
                rid.encode()
            }, rid = with(Encryption) {
                rid.encode()
            }, deviceGb = "A", telNo = tel, passWd = with(Encryption) {
                password.encrypt(secretKey)
            })
            authKey = loginResponse.item.authKey ?: throw Exception("Auth Key is null")
            Log.i("getAuthKey", "   >> Auth Key: $authKey")
            return authKey
        } catch (e: Exception) {
            Log.e("getAuthKey", e.toString())
            e.printStackTrace()
            return null
        }
    }

    suspend fun getQR(
        rid: String
    ): String? {
        val encryption = Encryption
        try {
            Log.i("getQR", "3. QR코드 데이터 요청 중...")
            val qrResponse = api.getQrCode(
                realId = with(encryption) {
                    rid.encode()
                }, authKey = authKey, newCheck = "Y"
            )
            qrData = qrResponse.item.qrCode ?: throw Exception("QR Code is null")

            Log.i("getQR", "===============================")
            Log.i("getQR", "QR Code Data: $qrData")
            Log.i("getQR", "===============================")
            return qrData
        } catch (e: Exception) {
            Log.e("getQR", e.toString())
            e.printStackTrace()
            return null
        }
    }
}


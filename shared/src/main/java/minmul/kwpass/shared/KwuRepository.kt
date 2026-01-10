package minmul.kwpass.shared

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import okio.IOException
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import timber.log.Timber
import javax.inject.Inject

@Xml(name = "root")
data class KwResponse(
    @param:Element(name = "item") val item: KwItem
)

@Xml(name = "item")
data class KwItem(
    @param:PropertyElement(name = "sec_key") val secret: String?,
    @param:PropertyElement(name = "auth_key") val authKey: String?,
    @param:PropertyElement(name = "qr_code") val qrCode: String?
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
                Timber.e("No Secret Key")
                throw KwPassException.ServerError()
            }

            val auth = getAuthKey(rid, password, tel)
            if (auth.isNullOrBlank()) {
                Timber.e("No Auth Key")
                throw KwPassException.AccountError()
            }


            val qr = getQR(rid)
            if (qr.isNullOrBlank()) {
                Timber.e("No QR")
                throw KwPassException.ServerError()
            }
            return qrData

        } catch (e: KwPassException) {
            throw e
        } catch (e: IOException) {
            Timber.e(e)
            throw KwPassException.NetworkError()
        } catch (e: Exception) {
            Timber.e(e, "알 수 없는 오류 발생")
            throw KwPassException.UnknownError()
        }
    }


    suspend fun getSecretKey(
        rid: String,
    ): String? {
        return try {
            Timber.tag("getSecretKey").i("1. 시크릿 키 요청 중...")
            val keyResponse = api.getSecretKey(
                userId = with(Encryption) {
                    rid.encode()
                })
            Timber.tag("getSecretKey").i("   >> Secret Key: $secretKey (${secretKey.length})")
            secretKey = keyResponse.item.secret ?: return null
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
    ): String? {
        return try {
            Timber.tag("getAuthKey").i("2. 로그인 요청 중...")
            val loginResponse = api.getAuthKey(realId = with(Encryption) {
                rid.encode()
            }, rid = with(Encryption) {
                rid.encode()
            }, deviceGb = "A", telNo = tel, passWd = with(Encryption) {
                password.encrypt(secretKey)
            })
            Timber.tag("getAuthKey").i("   >> Auth Key: $authKey (${authKey.length})")
            authKey = loginResponse.item.authKey ?: return null
            authKey
        } catch (e: Exception) {
            if (e is IOException) throw e
            Timber.e(e)
            null
        }
    }

    suspend fun getQR(
        rid: String
    ): String? {
        val encryption = Encryption
        return try {
            Timber.tag("getQR").i("3. QR코드 데이터 요청 중...")
            val qrResponse = api.getQrCode(
                realId = with(encryption) {
                    rid.encode()
                }, authKey = authKey, newCheck = "Y"
            )
            Timber.tag("getQR").i("===============================")
            Timber.tag("getQR").i("QR Code Data: $qrData (${qrData.length})")
            Timber.tag("getQR").i("===============================")
            qrData = qrResponse.item.qrCode ?: return null
            qrData
        } catch (e: Exception) {
            if (e is IOException) throw  e
            Timber.e(e)
            null
        }
    }
}


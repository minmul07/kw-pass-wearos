package minmul.kwpass.shared

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import okio.IOException
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import timber.log.Timber
import javax.inject.Inject

@Xml(name = "root")
data class KwResponse(
    @param:Element(name = "item") val item: KwItem,
)

@Xml(name = "item")
data class KwItem(
    @param:PropertyElement(name = "sec_key") val secret: String?,
    @param:PropertyElement(name = "auth_key") val authKey: String?,
    @param:PropertyElement(name = "qr_code") val qrCode: String?,
)

interface KwuApiService {
    @FormUrlEncoded
    @POST("mobile/MA/xml_user_key.php")
    suspend fun getSecretKey(
        @Header(NetworkProfilingListener.PROFILE_SESSION_HEADER) profileSessionId: String? = null,
        @Field("user_id") userId: String,
    ): KwResponse

    @FormUrlEncoded
    @POST("mobile/MA/xml_login_and.php")
    suspend fun getAuthKey(
        @Header(NetworkProfilingListener.PROFILE_SESSION_HEADER) profileSessionId: String? = null,
        @Field("real_id") realId: String,
        @Field("rid") rid: String,
        @Field("device_gb") deviceGb: String,
        @Field("tel_no") telNo: String,
        @Field("pass_wd") passWd: String,
    ): KwResponse

    @FormUrlEncoded
    @POST("mobile/MA/xml_userInfo_auth.php")
    suspend fun getQrCode(
        @Header(NetworkProfilingListener.PROFILE_SESSION_HEADER) profileSessionId: String? = null,
        @Field("real_id") realId: String,
        @Field("auth_key") authKey: String,
        @Field("new_check") newCheck: String,
    ): KwResponse
}

class KwuRepository @Inject constructor(
    private val kwuApiService: KwuApiService,
) {
    suspend fun getSecretKey(
        rid: String,
        profileSessionId: String? = null,
    ): String? {
        return try {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.secretKey.requestStart",
                    "ridLength=${rid.length}",
                )
            }
            Timber.tag("getSecretKey").i("1. secret key request started")

            val secretKeyResponse = kwuApiService.getSecretKey(
                profileSessionId = profileSessionId,
                userId = with(Encryption) {
                    rid.encode()
                },
            )

            val secretKey = secretKeyResponse.item.secret
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.secretKey.responseParsed",
                    "secretLength=${secretKey?.length ?: 0}",
                )
            }
            Timber.tag("getSecretKey")
                .i("   >> Secret Key length: ${secretKey?.length ?: "NULL"}")
            secretKey
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.secretKey.failed",
                    "error=${e.javaClass.simpleName}:${e.message}",
                )
            }
            if (e is IOException) throw e
            Timber.e(e)
            null
        }
    }

    suspend fun getAuthKey(
        rid: String,
        password: String,
        tel: String,
        secretKey: String,
        profileSessionId: String? = null,
    ): String? {
        return try {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.authKey.requestStart",
                    "ridLength=${rid.length}, telLength=${tel.length}, secretLength=${secretKey.length}",
                )
            }
            Timber.tag("getAuthKey").i("2. auth key request started")

            val authKeyResponse = kwuApiService.getAuthKey(
                profileSessionId = profileSessionId,
                realId = with(Encryption) {
                    rid.encode()
                },
                rid = with(Encryption) {
                    rid.encode()
                },
                deviceGb = "A",
                telNo = tel,
                passWd = with(Encryption) {
                    password.encrypt(secretKey)
                },
            )

            val authKey = authKeyResponse.item.authKey
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.authKey.responseParsed",
                    "authKeyLength=${authKey?.length ?: 0}",
                )
            }
            Timber.tag("getAuthKey")
                .i("   >> Auth Key length: ${authKey?.length ?: "NULL"}")
            authKey
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.authKey.failed",
                    "error=${e.javaClass.simpleName}:${e.message}",
                )
            }
            if (e is IOException) throw e
            Timber.e(e)
            null
        }
    }

    suspend fun getQrString(
        rid: String,
        authKey: String,
        profileSessionId: String? = null,
    ): String? {
        return try {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.qrCode.requestStart",
                    "ridLength=${rid.length}, authKeyLength=${authKey.length}",
                )
            }
            Timber.tag("getQR").i("3. qr code request started")

            val qrResponse = kwuApiService.getQrCode(
                profileSessionId = profileSessionId,
                realId = with(Encryption) {
                    rid.encode()
                },
                authKey = authKey,
                newCheck = "Y",
            )

            val qrString = qrResponse.item.qrCode
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.qrCode.responseParsed",
                    "qrLength=${qrString?.length ?: 0}",
                )
            }
            Timber.tag("getQR")
                .i("QR Code length: ${qrString?.length ?: "NULL"}")
            qrString
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "repository.qrCode.failed",
                    "error=${e.javaClass.simpleName}:${e.message}",
                )
            }
            if (e is IOException) throw e
            Timber.e(e)
            null
        }
    }
}

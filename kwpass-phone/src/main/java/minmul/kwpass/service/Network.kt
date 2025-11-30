package minmul.kwpass.service

import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class MemoryCookieJar : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: ArrayList()
    }
}

object Network {
    private const val BASE_URL = "https://mobileid.kw.ac.kr/" // 끝에 / 필수

    // TikXml 설정 (혹시 모를 파싱 에러 방지 위해 exceptionOnUnreadXml false 설정)
    private val tikXml = TikXml.Builder().exceptionOnUnreadXml(false).build()

    // OkHttpClient에 쿠키 저장소 연결
    private val client = OkHttpClient.Builder().cookieJar(MemoryCookieJar()).build()

    val api: KwuApiService =
        Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(
            TikXmlConverterFactory.create(tikXml))
            .build().create(KwuApiService::class.java)
}
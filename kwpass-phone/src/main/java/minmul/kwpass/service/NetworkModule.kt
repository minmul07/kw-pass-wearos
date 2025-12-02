package minmul.kwpass.service

import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

class MemoryCookieJar : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: ArrayList()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://mobileid.kw.ac.kr/" // 끝에 / 필수

    // TikXml 설정 (혹시 모를 파싱 에러 방지 위해 exceptionOnUnreadXml false 설정)
    private val tikXml = TikXml.Builder().exceptionOnUnreadXml(false).build()

    // OkHttpClient에 쿠키 저장소 연결
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().cookieJar(MemoryCookieJar()).build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit { // Hilt가 OkHttpClient를 알아서 넣어준대요 신기방기
        return Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(
            TikXmlConverterFactory.create(tikXml)
        ).build()
    }


    @Provides
    @Singleton
    fun provideKwuApiService(retrofit: Retrofit): KwuApiService { // Hilt가 Retrofit을 넣어준대요 신기방기
        return retrofit.create(KwuApiService::class.java) // hilt에게 생성방식 명시
    }
}
package minmul.kwpass.shared

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

// 세션 쿠키로 도서관 서버 통신 시 연속성 보장
class MemoryCookieJar : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()

    // 서버로부터 set-cookie 헤더가 오면 cookieStore에 저장
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    // 서버에 요청보낼때 저장된 쿠키 있으면 헤더에 포함해서 전송
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: ArrayList()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://mobileid.kw.ac.kr/" // 끝에 / 필수

    // TikXml 설정 (데이터클래스에 선언되지 않은 태그들 무시를 위해 exceptionOnUnreadXml false 설정)
    private val tikXml = TikXml.Builder().exceptionOnUnreadXml(false).build()

    // 1단계: OkHttpClient 생성 및 쿠키 저장소 연결
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().cookieJar(MemoryCookieJar()).build()
    }

    // 2단계: Retrofit 생성 - OkHttpClient에 baseUrl 연결하고 xml 변환기 연결
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit { // Hilt가 OkHttpClient를 알아서 넣어준대요 신기방기
        return Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(
            TikXmlConverterFactory.create(tikXml)
        ).build()
    }

    // 3단계: 최종 ApiService 생성, Retrofit이 KwuApiService의 구현체를 만들어줌
    // 다른 곳에서 주입받아서 사용
    @Provides
    @Singleton
    fun provideKwuApiService(retrofit: Retrofit): KwuApiService { // Hilt가 Retrofit을 넣어준대요 신기방기
        return retrofit.create(KwuApiService::class.java) // hilt에게 생성방식 명시
    }
}
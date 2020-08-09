package com.example.youtube

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MasterApplication : Application() {

    lateinit var service: RetrofitService

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        createRetrofit()
        // chrome://inspect/#device
    }

    fun createRetrofit() {
        val header = Interceptor {
            val original = it.request()                     // 원래 신호
            if (checkIsLogin()) {
                getUserToken()?.let { token ->           // null 이 아닌경우에 헤더에 token 을 넣겠다
                    val request = original.newBuilder()             // 빌더로 원래 신호 변경
                        .header("Authorization", "token " + token)    // 헤더 작성
                        .build()                                    // 만들기, 실행
                    it.proceed(request)
                }
            } else {
                it.proceed(original)
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(header)                         // 헤더를 추가한 신호 인터셉트함
            .addNetworkInterceptor(StethoInterceptor())     // 스테토를 만들어줌
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)         // 클라이언트 = 스테토 ( 네트워크 확인 )
            .build()
        service = retrofit.create(RetrofitService::class.java)
    }

    fun checkIsLogin(): Boolean {
        val sp = getSharedPreferences("login_sp", Context.MODE_PRIVATE)
        val token = sp.getString("login_sp", "null")
        if (token != "null") return true
        else return false
    }

    fun getUserToken(): String? {
        val sp = getSharedPreferences("login_sp", Context.MODE_PRIVATE)
        val token = sp.getString("login_sp", "null")
        if (token == "null") return null
        else return token
    }


}
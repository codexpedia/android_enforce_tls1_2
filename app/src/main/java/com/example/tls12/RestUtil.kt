package com.example.tls12

import android.os.Build
import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.util.ArrayList
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class RestUtil private constructor() {

    companion object {
        private val API_BASE_URL = "https://api.github.com/"
        private var self: RestUtil? = null
        val instance: RestUtil
            get() {
                if (self == null) {
                    synchronized(RestUtil::class.java) {
                        if (self == null) {
                            self = RestUtil()
                        }
                    }
                }
                return self!!
            }
    }

    val retrofit: Retrofit

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClientBuilder = createOkhttpClientBuilderWithTlsConfig()
        val httpClient = httpClientBuilder.addInterceptor(interceptor).build()

        val builder = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())

        retrofit = builder.client(httpClient).build()
    }

    private fun createOkhttpClientBuilderWithTlsConfig(): OkHttpClient.Builder {
        return OkHttpClient.Builder().apply {
            val trustManager by lazy {
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                trustManagerFactory.trustManagers.first { it is X509TrustManager } as X509TrustManager
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                try {
                    val sc = SSLContext.getInstance(TlsVersion.TLS_1_1.javaName())
                    sc.init(null, null, null)
                    sslSocketFactory(TlsSocketFactory(sc.socketFactory), trustManager)

                    val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(*TlsSocketFactory.ALLOWED_TLS_VERSIONS)
                        .build()

                    val specs = ArrayList<ConnectionSpec>()
                    specs.add(cs)
                    specs.add(ConnectionSpec.COMPATIBLE_TLS)
                    specs.add(ConnectionSpec.CLEARTEXT)

                    connectionSpecs(specs)
                } catch (exc: Exception) {
                    Log.e("OkHttpTLSCompat", "Error while setting TLS 1.1 and 1.2", exc)
                }

            }
        }
    }

}
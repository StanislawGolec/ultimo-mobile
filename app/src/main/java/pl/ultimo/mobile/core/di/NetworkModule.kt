package pl.ultimo.mobile.core.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.ultimo.mobile.BuildConfig
import pl.ultimo.mobile.core.network.AndroidNetworkConnectivityObserver
import pl.ultimo.mobile.core.network.NetworkConnectivityObserver
import pl.ultimo.mobile.core.network.OkHttpWebSocketEngine
import pl.ultimo.mobile.core.network.WebSocketEngine
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityObserver(
        observer: AndroidNetworkConnectivityObserver
    ): NetworkConnectivityObserver

    @Binds
    @Singleton
    abstract fun bindWebSocketEngine(
        engine: OkHttpWebSocketEngine
    ): WebSocketEngine

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS) // 0 keeps persistent WebSockets alive
                .retryOnConnectionFailure(true)

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
                builder.addInterceptor(loggingInterceptor)
            }

            return builder.build()
        }
    }
}
